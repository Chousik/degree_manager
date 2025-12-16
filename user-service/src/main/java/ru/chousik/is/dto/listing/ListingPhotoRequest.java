package ru.chousik.is.dto.listing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ListingPhotoRequest(
        @NotBlank @Size(max = 255) String url,
        Short sortOrder
) {
}
