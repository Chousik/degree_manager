package ru.chousik.is.admin.dto.listing;

import java.util.UUID;

public record ListingPhotoDto(
        UUID id,
        String url,
        Short sortOrder
) {
}
