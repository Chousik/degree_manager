package ru.chousik.is.dto.review;

import ru.chousik.is.entity.ReviewAuthorRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID rentalId,
        ReviewAuthorRole authorRole,
        short rating,
        String text,
        OffsetDateTime createdAt
) {
}
