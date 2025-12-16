package ru.chousik.is.dto.moderation;

import ru.chousik.is.entity.ReviewAuthorRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FlaggedReviewDto(
        UUID id,
        UUID rentalId,
        ReviewAuthorRole authorRole,
        short rating,
        String text,
        String flagReason,
        OffsetDateTime createdAt
) {
}
