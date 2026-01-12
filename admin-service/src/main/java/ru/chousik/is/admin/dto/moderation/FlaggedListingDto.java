package ru.chousik.is.admin.dto.moderation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FlaggedListingDto(
        UUID id,
        String title,
        BigDecimal pricePerHour,
        String flagReason,
        OffsetDateTime createdAt
) {
}
