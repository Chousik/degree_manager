package ru.chousik.is.admin.dto.listing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ListingDetailDto(
        UUID id,
        UUID ownerId,
        String title,
        String description,
        BigDecimal pricePerHour,
        BigDecimal depositAmount,
        boolean autoConfirmation,
        String status,
        BigDecimal latitude,
        BigDecimal longitude,
        OffsetDateTime createdAt,
        List<AvailabilitySlotDto> availabilitySlots,
        List<ListingPhotoDto> photos,
        List<CategorySummaryDto> categories,
        Boolean flagged,
        String flagReason
) {
}
