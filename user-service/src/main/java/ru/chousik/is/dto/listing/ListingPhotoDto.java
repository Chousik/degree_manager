package ru.chousik.is.dto.listing;

import java.util.UUID;

public record ListingPhotoDto(
        UUID id,
        String url,
        Short sortOrder
) {
}
