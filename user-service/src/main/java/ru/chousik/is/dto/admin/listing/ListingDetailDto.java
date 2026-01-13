package ru.chousik.is.dto.admin.listing;

import ru.chousik.is.dto.listing.AvailabilitySlotDto;
import ru.chousik.is.dto.listing.CategorySummaryDto;
import ru.chousik.is.dto.listing.ListingPhotoDto;
import ru.chousik.is.entity.ListingStatus;

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
        ListingStatus status,
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
