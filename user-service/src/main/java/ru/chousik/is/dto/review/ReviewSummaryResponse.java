package ru.chousik.is.dto.review;

import ru.chousik.is.entity.RentalStatus;
import ru.chousik.is.entity.ReviewAuthorRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewSummaryResponse(
        UUID id,
        UUID rentalId,
        UUID listingId,
        String listingTitle,
        ReviewAuthorRole authorRole,
        int rating,
        String text,
        RentalStatus rentalStatus,
        OffsetDateTime createdAt
) {
}
