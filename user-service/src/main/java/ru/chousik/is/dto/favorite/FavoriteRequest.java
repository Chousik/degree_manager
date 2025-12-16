package ru.chousik.is.dto.favorite;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FavoriteRequest(
        @NotNull UUID userId,
        @NotNull UUID listingId
) {
}
