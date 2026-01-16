package ru.chousik.is.dto.rental;

import ru.chousik.is.entity.RentalStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RentalResponse(
        UUID id,
        UUID listingId,
        UUID lessorId,
        UUID lesseeId,
        RentalStatus status,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        BigDecimal totalAmount,
        BigDecimal depositAmount,
        OffsetDateTime createdAt,
        OffsetDateTime confirmationDeadline,
        UUID completionRequestedBy,
        UUID completionConfirmedBy,
        UUID cancellationRequestedBy,
        UUID contractId,
        String contractStatus,
        String contractFileUrl,
        UUID paymentId,
        String paymentConfirmationUrl
) {
}
