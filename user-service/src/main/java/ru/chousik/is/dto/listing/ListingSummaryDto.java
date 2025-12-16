package ru.chousik.is.dto.listing;

import ru.chousik.is.entity.ListingStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ListingSummaryDto(
        UUID id,
        String title,
        String description,
        BigDecimal pricePerHour,
        BigDecimal depositAmount,
        ListingStatus status,
        BigDecimal latitude,
        BigDecimal longitude,
        String previewPhotoUrl
) {
}
