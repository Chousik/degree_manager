package ru.chousik.is.dto.admin.review;

import ru.chousik.is.entity.ReviewAuthorRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewDetailDto(
        UUID id,
        UUID listingId,
        UUID rentalId,
        UUID lessorId,
        UUID lesseeId,
        ReviewAuthorRole authorRole,
        Short rating,
        String text,
        Boolean flagged,
        String flagReason,
        Boolean hidden,
        OffsetDateTime createdAt
) {
}
