package ru.chousik.is.admin.dto.review;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewDetailDto(
        UUID id,
        UUID listingId,
        UUID rentalId,
        UUID lessorId,
        UUID lesseeId,
        String authorRole,
        Short rating,
        String text,
        Boolean flagged,
        String flagReason,
        Boolean hidden,
        OffsetDateTime createdAt
) {
}
