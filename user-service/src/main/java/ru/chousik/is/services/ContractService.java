package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.contract.ContractResponse;
import ru.chousik.is.entity.Contract;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.ContractRepository;
import ru.chousik.is.repository.RentalRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ContractRepository contractRepository;
    private final RentalRepository rentalRepository;

    @Transactional(readOnly = true)
    public Contract getContractForRental(UUID rentalId, UUID userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental %s not found".formatted(rentalId)));
        ensureParticipant(rental, userId);
        return contractRepository.findFirstByRental_IdOrderBySignedAtDesc(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract for rental %s not found".formatted(rentalId)));
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractResponse(UUID rentalId, UUID userId) {
        Contract contract = getContractForRental(rentalId, userId);
        return new ContractResponse(
                contract.getId(),
                contract.getRental() != null ? contract.getRental().getId() : null,
                contract.getStatus(),
                contract.getSignedAt(),
                contract.getFileUrl(),
                contract.getSignatureHash()
        );
    }

    @Transactional(readOnly = true)
    public String renderContractText(UUID rentalId, UUID userId) {
        Contract contract = getContractForRental(rentalId, userId);
        Rental rental = contract.getRental();
        Listing listing = rental != null ? rental.getListing() : null;
        User lessor = rental != null ? rental.getLessor() : null;
        User lessee = rental != null ? rental.getLessee() : null;

        StringBuilder sb = new StringBuilder();
        sb.append("ДОГОВОР АРЕНДЫ № ").append(rentalId).append('\n');
        sb.append("Дата подписания: ").append(formatDateTime(contract.getSignedAt())).append('\n');
        sb.append('\n');
        sb.append("Арендодатель: ").append(formatUser(lessor)).append('\n');
        sb.append("Арендатор: ").append(formatUser(lessee)).append('\n');
        sb.append('\n');
        sb.append("Предмет аренды: ").append(listing != null ? safe(listing.getTitle()) : "—").append('\n');
        sb.append("Адрес: ").append(listing != null ? safe(listing.getAddress()) : "—").append('\n');
        sb.append("Период: ").append(formatDate(rental != null ? rental.getStartAt() : null))
                .append(" — ")
                .append(formatDate(rental != null ? rental.getEndAt() : null))
                .append('\n');
        sb.append("Стоимость аренды: ").append(formatMoney(rental != null ? rental.getTotalAmount() : null)).append('\n');
        sb.append("Залог: ").append(formatMoney(rental != null ? rental.getDepositAmount() : null)).append('\n');
        sb.append('\n');
        sb.append("Электронная подпись: ").append(contract.getSignatureHash() != null ? contract.getSignatureHash() : "—");
        return sb.toString();
    }

    private void ensureParticipant(Rental rental, UUID userId) {
        if (rental == null) {
            throw new ResourceNotFoundException("Rental not found");
        }
        UUID lessorId = rental.getLessor() != null ? rental.getLessor().getId() : null;
        UUID lesseeId = rental.getLessee() != null ? rental.getLessee().getId() : null;
        if (!Objects.equals(lessorId, userId) && !Objects.equals(lesseeId, userId)) {
            throw new BusinessValidationException("User is not a participant of rental");
        }
    }

    private String formatUser(User user) {
        if (user == null) {
            return "—";
        }
        String name = (safe(user.getName()) + " " + safe(user.getSurname())).trim();
        String username = user.getUsername() != null ? "(@" + user.getUsername() + ")" : "";
        return (name.isBlank() ? "Пользователь" : name) + " " + username + " [" + user.getId() + "]";
    }

    private String formatDate(OffsetDateTime value) {
        return value != null ? DATE.format(value) : "—";
    }

    private String formatDateTime(OffsetDateTime value) {
        return value != null ? DATE_TIME.format(value) : "—";
    }

    private String formatMoney(BigDecimal value) {
        return value != null ? value.toPlainString() + " ₽" : "—";
    }

    private String safe(String value) {
        return value == null ? "—" : value;
    }
}
