package ru.chousik.is.dto.rental;

import ru.chousik.is.entity.RentalStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RentalOwnerSummary(
        UUID rentalId,
        UUID listingId,
        String listingTitle,
        UUID lesseeId,
        String lesseeName,
        String lesseeUsername,
        RentalStatus status,
        UUID completionRequestedBy,
        String contractFileUrl,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        OffsetDateTime createdAt
) {
}
