package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.rental.RentalCreateRequest;
import ru.chousik.is.dto.rental.RentalDateRange;
import ru.chousik.is.dto.rental.RentalOwnerSummary;
import ru.chousik.is.dto.rental.RentalResponse;
import ru.chousik.is.dto.rental.RentalUserSummary;
import ru.chousik.is.entity.Contract;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.Payment;
import ru.chousik.is.entity.PaymentPurpose;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.RentalStatus;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.ContractRepository;
import ru.chousik.is.repository.ListingRepository;
import ru.chousik.is.repository.PaymentRepository;
import ru.chousik.is.repository.RentalRepository;
import ru.chousik.is.repository.UserRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalService {

    private static final Duration CONFIRMATION_WINDOW = Duration.ofHours(24);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final RentalRepository rentalRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ConversationService conversationService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    public RentalResponse createRental(RentalCreateRequest request) {
        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing %s not found".formatted(request.listingId())));
        User lessee = userRepository.findById(request.lesseeId())
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(request.lesseeId())));
        User lessor = listing.getOwner();
        if (lessor == null) {
            throw new BusinessValidationException("Listing owner is not set");
        }
        if (Objects.equals(lessor.getId(), lessee.getId())) {
            throw new BusinessValidationException("Owner cannot rent own listing");
        }
        LocalDate startDate = request.startAt().toLocalDate();
        LocalDate endDate = request.endAt().toLocalDate();
        validateDates(startDate, endDate);
        OffsetDateTime startAtUtc = toUtc(startDate.atStartOfDay());
        OffsetDateTime endAtUtc = toUtc(endDate.atStartOfDay());
        boolean hasOverlap = rentalRepository.existsByListing_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                listing.getId(),
                List.of(RentalStatus.PENDING, RentalStatus.ACTIVE, RentalStatus.COMPLETION_PENDING),
                endAtUtc,
                startAtUtc
        );
        if (hasOverlap) {
            throw new BusinessValidationException("Даты пересекаются с другой арендой");
        }

        Rental rental = new Rental();
        rental.setListing(listing);
        rental.setLessor(lessor);
        rental.setLessee(lessee);
        rental.setStartAt(startAtUtc);
        rental.setEndAt(endAtUtc);
        rental.setCreatedAt(OffsetDateTime.now());
        rental.setDepositAmount(resolveDeposit(request, listing));
        rental.setTotalAmount(calculateTotalAmount(listing, startDate, endDate));

        boolean autoConfirmation = Boolean.TRUE.equals(listing.getAutoConfirmation());
        rental.setStatus(autoConfirmation ? RentalStatus.ACTIVE : RentalStatus.PENDING);
        Rental saved = rentalRepository.save(rental);
        paymentService.preparePaymentRecord(saved, PaymentPurpose.RENTAL, rental.getTotalAmount());
        if (rental.getDepositAmount() != null && rental.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            paymentService.preparePaymentRecord(saved, PaymentPurpose.DEPOSIT, rental.getDepositAmount());
        }
        notifyOwnerAboutRental(saved);

        Payment initiatedPayment = null;
        if (autoConfirmation) {
            activateRental(saved);
            initiatedPayment = paymentService.initiatePayment(saved.getId(), PaymentPurpose.RENTAL, null);
        }
        return initiatedPayment != null ? mapToResponse(saved, initiatedPayment) : mapToResponse(saved);
    }

    public RentalResponse confirmRental(UUID rentalId, UUID ownerId) {
        Rental rental = findRental(rentalId);
        ensureLessor(rental, ownerId);
        ensureStatus(rental, RentalStatus.PENDING);
        if (isExpired(rental)) {
            rental.setStatus(RentalStatus.CANCELLED);
            return mapToResponse(rental);
        }
        activateRental(rental);
        return mapToResponse(rental);
    }

    public RentalResponse cancelRental(UUID rentalId, UUID actorId) {
        Rental rental = findRental(rentalId);
        ensureParticipant(rental, actorId);
        if (rental.getStatus() == RentalStatus.COMPLETED || rental.getStatus() == RentalStatus.CANCELLED) {
            throw new BusinessValidationException("Rental already finished");
        }
        rental.setCancellationRequestedBy(actorId);
        rental.setCancellationRequestedAt(OffsetDateTime.now());
        rental.setStatus(RentalStatus.CANCELLED);
        return mapToResponse(rental);
    }

    public RentalResponse completeRental(UUID rentalId, UUID actorId) {
        Rental rental = findRental(rentalId);
        ensureParticipant(rental, actorId);
        if (rental.getStatus() == RentalStatus.COMPLETED || rental.getStatus() == RentalStatus.CANCELLED) {
            throw new BusinessValidationException("Rental already finished");
        }
        if (rental.getStatus() == RentalStatus.ACTIVE) {
            if (Objects.equals(rental.getCompletionRequestedBy(), actorId)) {
                throw new BusinessValidationException("Completion already requested");
            }
            rental.setCompletionRequestedBy(actorId);
            rental.setCompletionRequestedAt(OffsetDateTime.now());
            rental.setStatus(RentalStatus.COMPLETION_PENDING);
            return mapToResponse(rental);
        }
        if (rental.getStatus() == RentalStatus.COMPLETION_PENDING) {
            UUID requester = rental.getCompletionRequestedBy();
            if (requester == null) {
                rental.setCompletionRequestedBy(actorId);
                rental.setCompletionRequestedAt(OffsetDateTime.now());
                return mapToResponse(rental);
            }
            if (Objects.equals(requester, actorId)) {
                throw new BusinessValidationException("Waiting for second confirmation");
            }
            rental.setCompletionConfirmedBy(actorId);
            rental.setCompletionConfirmedAt(OffsetDateTime.now());
            rental.setStatus(RentalStatus.COMPLETED);
            paymentService.refundDepositIfCompleted(rental);
            return mapToResponse(rental);
        }
        throw new BusinessValidationException("Rental status must be ACTIVE");
    }

    @Transactional(readOnly = true)
    public List<RentalOwnerSummary> getOwnerUpcomingRentals(UUID ownerId) {
        List<Rental> rentals = rentalRepository.findByLessor_IdAndStatusInOrderByStartAtAsc(
                ownerId,
                List.of(RentalStatus.PENDING, RentalStatus.ACTIVE, RentalStatus.COMPLETION_PENDING)
        );
        return rentals.stream()
                .map(this::mapToOwnerSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RentalDateRange> getListingActiveRanges(UUID listingId) {
        List<Rental> rentals = rentalRepository.findByListing_IdAndStatusInOrderByStartAtAsc(
                listingId,
                List.of(RentalStatus.PENDING, RentalStatus.ACTIVE, RentalStatus.COMPLETION_PENDING)
        );
        return rentals.stream()
                .map(rental -> new RentalDateRange(rental.getStartAt(), rental.getEndAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RentalUserSummary> getUserRentals(UUID userId) {
        List<Rental> asLessor = rentalRepository.findByLessor_IdOrderByStartAtDesc(userId);
        List<Rental> asLessee = rentalRepository.findByLessee_IdOrderByStartAtDesc(userId);
        List<RentalUserSummary> summaries = new java.util.ArrayList<>();
        asLessor.forEach(rental -> summaries.add(mapToUserSummary(rental, "LESSOR")));
        asLessee.forEach(rental -> summaries.add(mapToUserSummary(rental, "LESSEE")));
        summaries.sort((a, b) -> {
            if (a.startAt() == null && b.startAt() == null) return 0;
            if (a.startAt() == null) return 1;
            if (b.startAt() == null) return -1;
            return b.startAt().compareTo(a.startAt());
        });
        return summaries;
    }

    public Rental findRental(UUID rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental %s not found".formatted(rentalId)));
    }

    private void activateRental(Rental rental) {
        rental.setStatus(RentalStatus.ACTIVE);
        rentalRepository.save(rental);
        createContract(rental);
        conversationService.ensureConversation(rental);
        if (rental.getDepositAmount() != null && rental.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            paymentService.preparePaymentRecord(rental, PaymentPurpose.DEPOSIT, rental.getDepositAmount());
        }
    }

    private void createContract(Rental rental) {
        Contract contract = new Contract();
        contract.setRental(rental);
        contract.setStatus("signed");
        contract.setSignedAt(OffsetDateTime.now());
        contract.setFileUrl("/contracts/" + rental.getId() + ".pdf");
        contract.setSignatureHash(generateSignature(rental));
        contractRepository.save(contract);
    }

    private BigDecimal calculateTotalAmount(Listing listing, LocalDate start, LocalDate end) {
        long days = Duration.between(start.atStartOfDay(), end.atStartOfDay()).toDays();
        if (days <= 0) {
            throw new BusinessValidationException("Длительность аренды должна быть минимум один день");
        }
        BigDecimal price = listing.getPricePerHour();
        if (price == null) {
            throw new BusinessValidationException("Listing price is not set");
        }
        return price.multiply(BigDecimal.valueOf(days));
    }

    private BigDecimal resolveDeposit(RentalCreateRequest request, Listing listing) {
        BigDecimal requestedDeposit = request.depositAmount();
        if (requestedDeposit != null && requestedDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException("Deposit must be positive");
        }
        if (requestedDeposit != null) {
            return requestedDeposit;
        }
        return listing.getDepositAmount();
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (!end.isAfter(start)) {
            throw new BusinessValidationException("Дата возврата должна быть позже даты начала");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new BusinessValidationException("Дата начала должна быть в будущем");
        }
    }

    private OffsetDateTime toUtc(LocalDateTime localDateTime) {
        return localDateTime.atOffset(ZoneOffset.UTC);
    }

    private void ensureParticipant(Rental rental, UUID actorId) {
        UUID lessorId = rental.getLessor() != null ? rental.getLessor().getId() : null;
        UUID lesseeId = rental.getLessee() != null ? rental.getLessee().getId() : null;
        if (!Objects.equals(actorId, lessorId) && !Objects.equals(actorId, lesseeId)) {
            throw new BusinessValidationException("User is not a participant of rental");
        }
    }

    private void ensureLessor(Rental rental, UUID actorId) {
        UUID lessorId = rental.getLessor() != null ? rental.getLessor().getId() : null;
        if (!Objects.equals(actorId, lessorId)) {
            throw new BusinessValidationException("Only listing owner can confirm rental");
        }
    }

    private void ensureStatus(Rental rental, RentalStatus expected) {
        if (rental.getStatus() != expected) {
            throw new BusinessValidationException("Rental status must be %s".formatted(expected));
        }
    }

    private boolean isExpired(Rental rental) {
        OffsetDateTime createdAt = rental.getCreatedAt();
        if (createdAt == null) {
            return false;
        }
        return createdAt.plus(CONFIRMATION_WINDOW).isBefore(OffsetDateTime.now());
    }

    private String generateSignature(Rental rental) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(rental.getId().toString().getBytes(StandardCharsets.UTF_8));
            if (rental.getLessor() != null) {
                digest.update(rental.getLessor().getId().toString().getBytes(StandardCharsets.UTF_8));
            }
            if (rental.getLessee() != null) {
                digest.update(rental.getLessee().getId().toString().getBytes(StandardCharsets.UTF_8));
            }
            digest.update(OffsetDateTime.now().toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private RentalResponse mapToResponse(Rental rental) {
        return mapToResponse(rental, null);
    }

    private RentalResponse mapToResponse(Rental rental, Payment payment) {
        OffsetDateTime deadline = rental.getCreatedAt() != null
                ? rental.getCreatedAt().plus(CONFIRMATION_WINDOW)
                : null;
        var contract = contractRepository.findFirstByRental_IdOrderBySignedAtDesc(rental.getId());
        return new RentalResponse(
                rental.getId(),
                rental.getListing() != null ? rental.getListing().getId() : null,
                rental.getLessor() != null ? rental.getLessor().getId() : null,
                rental.getLessee() != null ? rental.getLessee().getId() : null,
                rental.getStatus(),
                rental.getStartAt(),
                rental.getEndAt(),
                rental.getTotalAmount(),
                rental.getDepositAmount(),
                rental.getCreatedAt(),
                deadline,
                rental.getCompletionRequestedBy(),
                rental.getCompletionConfirmedBy(),
                rental.getCancellationRequestedBy(),
                contract.map(Contract::getId).orElse(null),
                contract.map(Contract::getStatus).orElse(null),
                contract.map(Contract::getFileUrl).orElse(null),
                payment != null ? payment.getId() : null,
                payment != null ? payment.getConfirmationUrl() : null
        );
    }

    private RentalOwnerSummary mapToOwnerSummary(Rental rental) {
        String lesseeName = rental.getLessee() != null
                ? String.format("%s %s", rental.getLessee().getName(), rental.getLessee().getSurname()).trim()
                : "Пользователь";
        String lesseeUsername = rental.getLessee() != null ? rental.getLessee().getUsername() : null;
        return new RentalOwnerSummary(
                rental.getId(),
                rental.getListing() != null ? rental.getListing().getId() : null,
                rental.getListing() != null ? rental.getListing().getTitle() : null,
                rental.getLessee() != null ? rental.getLessee().getId() : null,
                lesseeName,
                lesseeUsername,
                rental.getStatus(),
                rental.getCompletionRequestedBy(),
                rental.getStartAt(),
                rental.getEndAt(),
                rental.getCreatedAt()
        );
    }

    private void notifyOwnerAboutRental(Rental rental) {
        if (rental == null || rental.getListing() == null) {
            return;
        }
        User owner = rental.getListing().getOwner();
        User lessee = rental.getLessee();
        String lesseeLabel = lessee != null
                ? String.format("%s %s (@%s)", lessee.getName(), lessee.getSurname(), lessee.getUsername())
                : "Пользователь";
        String start = rental.getStartAt() != null ? rental.getStartAt().format(DATE_TIME_FORMATTER) : "—";
        String end = rental.getEndAt() != null ? rental.getEndAt().format(DATE_TIME_FORMATTER) : "—";
        String statusLabel = switch (rental.getStatus()) {
            case PENDING -> "ожидает подтверждения";
            case ACTIVE -> "подтверждено";
            case COMPLETION_PENDING -> "ожидает завершения";
            case COMPLETED -> "завершено";
            case CANCELLED -> "отменено";
        };
        String listingTitle = rental.getListing().getTitle() != null ? rental.getListing().getTitle() : "ваш инструмент";
        String body = String.format(
                "Новая аренда для \"%s\". Арендатор: %s. Период: %s — %s. Статус: %s.",
                listingTitle,
                lesseeLabel,
                start,
                end,
                statusLabel
        );
        notificationService.createRentalNotification(owner, body);
    }

    private RentalUserSummary mapToUserSummary(Rental rental, String role) {
        User counterparty = "LESSOR".equals(role) ? rental.getLessee() : rental.getLessor();
        String counterpartyName = counterparty != null
                ? String.format("%s %s", counterparty.getName(), counterparty.getSurname()).trim()
                : "Пользователь";
        String counterpartyUsername = counterparty != null ? counterparty.getUsername() : null;
        Payment deposit = paymentRepository.findFirstByRental_IdAndPurpose(rental.getId(), PaymentPurpose.DEPOSIT);
        Payment rentalPayment = paymentRepository.findFirstByRental_IdAndPurpose(rental.getId(), PaymentPurpose.RENTAL);
        String depositStatus = deposit != null ? deposit.getStatus() : null;
        String rentalStatus = rentalPayment != null ? rentalPayment.getStatus() : null;
        return new RentalUserSummary(
                rental.getId(),
                rental.getListing() != null ? rental.getListing().getId() : null,
                rental.getListing() != null ? rental.getListing().getTitle() : null,
                role,
                counterparty != null ? counterparty.getId() : null,
                counterpartyName,
                counterpartyUsername,
                rental.getStatus(),
                rental.getCompletionRequestedBy(),
                rental.getStartAt(),
                rental.getEndAt(),
                rental.getCreatedAt(),
                rental.getTotalAmount(),
                rental.getDepositAmount(),
                depositStatus,
                rentalStatus
        );
    }
}
