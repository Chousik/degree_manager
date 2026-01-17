package ru.chousik.is.dto.rental;

import ru.chousik.is.entity.RentalStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RentalUserSummary(
        UUID rentalId,
        UUID listingId,
        String listingTitle,
        String role,
        UUID counterpartyId,
        String counterpartyName,
        String counterpartyUsername,
        RentalStatus status,
        UUID completionRequestedBy,
        String contractFileUrl,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        OffsetDateTime createdAt,
        BigDecimal totalAmount,
        BigDecimal depositAmount,
        String depositStatus,
        String rentalStatus
) {
}
