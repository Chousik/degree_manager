package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.rental.RentalCreateRequest;
import ru.chousik.is.dto.rental.RentalResponse;
import ru.chousik.is.entity.Contract;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.PaymentPurpose;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.RentalStatus;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.ContractRepository;
import ru.chousik.is.repository.ListingRepository;
import ru.chousik.is.repository.RentalRepository;
import ru.chousik.is.repository.UserRepository;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.exceptions.ResourceNotFoundException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalService {

    private static final Duration CONFIRMATION_WINDOW = Duration.ofHours(24);

    private final RentalRepository rentalRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ConversationService conversationService;
    private final PaymentService paymentService;

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
        validateDates(request.startAt(), request.endAt());

        Rental rental = new Rental();
        rental.setListing(listing);
        rental.setLessor(lessor);
        rental.setLessee(lessee);
        rental.setStartAt(request.startAt());
        rental.setEndAt(request.endAt());
        rental.setCreatedAt(OffsetDateTime.now());
        rental.setDepositAmount(resolveDeposit(request, listing));
        rental.setTotalAmount(calculateTotalAmount(listing, request.startAt(), request.endAt()));

        boolean autoConfirmation = Boolean.TRUE.equals(listing.getAutoConfirmation());
        rental.setStatus(autoConfirmation ? RentalStatus.ACTIVE : RentalStatus.PENDING);
        Rental saved = rentalRepository.save(rental);
        paymentService.preparePaymentRecord(saved, PaymentPurpose.RENTAL, rental.getTotalAmount());
        if (rental.getDepositAmount() != null && rental.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            paymentService.preparePaymentRecord(saved, PaymentPurpose.DEPOSIT, rental.getDepositAmount());
        }

        if (autoConfirmation) {
            activateRental(saved);
        }
        return mapToResponse(saved);
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
        rental.setStatus(RentalStatus.CANCELLED);
        return mapToResponse(rental);
    }

    public RentalResponse completeRental(UUID rentalId, UUID actorId) {
        Rental rental = findRental(rentalId);
        ensureParticipant(rental, actorId);
        ensureStatus(rental, RentalStatus.ACTIVE);
        rental.setStatus(RentalStatus.COMPLETED);
        paymentService.refundDepositIfCompleted(rental);
        return mapToResponse(rental);
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

    private BigDecimal calculateTotalAmount(Listing listing, OffsetDateTime start, OffsetDateTime end) {
        long hours = Duration.between(start, end).toHours();
        if (hours <= 0) {
            throw new BusinessValidationException("Rental duration must be positive");
        }
        BigDecimal price = listing.getPricePerHour();
        if (price == null) {
            throw new BusinessValidationException("Listing price is not set");
        }
        return price.multiply(BigDecimal.valueOf(hours));
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

    private void validateDates(OffsetDateTime start, OffsetDateTime end) {
        if (!end.isAfter(start)) {
            throw new BusinessValidationException("End date must be after start date");
        }
        if (start.isBefore(OffsetDateTime.now())) {
            throw new BusinessValidationException("Start date must be in future");
        }
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
                contract.map(Contract::getId).orElse(null),
                contract.map(Contract::getStatus).orElse(null),
                contract.map(Contract::getFileUrl).orElse(null)
        );
    }
}
