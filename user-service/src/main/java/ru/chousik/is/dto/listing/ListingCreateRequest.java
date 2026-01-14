package ru.chousik.is.dto.listing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ListingCreateRequest(
        @NotNull UUID ownerId,
        @NotBlank @Size(max = 500) String title,
        @Size(max = 10000) String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal pricePerHour,
        @DecimalMin(value = "0.0") BigDecimal depositAmount,
        Boolean autoConfirmation,
        @Digits(integer = 3, fraction = 6) BigDecimal latitude,
        @Digits(integer = 3, fraction = 6) BigDecimal longitude,
        @Size(max = 500) String address,
        @Valid List<AvailabilitySlotRequest> availabilitySlots,
        @Valid List<ListingPhotoRequest> photos,
        List<UUID> categoryIds
) {
}
