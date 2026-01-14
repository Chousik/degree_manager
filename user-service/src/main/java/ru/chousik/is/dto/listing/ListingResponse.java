package ru.chousik.is.dto.listing;

import ru.chousik.is.entity.ListingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ListingResponse(
        UUID id,
        UUID ownerId,
        String title,
        String description,
        BigDecimal pricePerHour,
        BigDecimal depositAmount,
        boolean autoConfirmation,
        ListingStatus status,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        OffsetDateTime createdAt,
        List<AvailabilitySlotDto> availabilitySlots,
        List<ListingPhotoDto> photos,
        List<CategorySummaryDto> categories
) {
}
