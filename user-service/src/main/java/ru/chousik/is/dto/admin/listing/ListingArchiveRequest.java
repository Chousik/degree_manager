package ru.chousik.is.dto.admin.listing;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ListingArchiveRequest(
        @NotNull UUID adminId,
        String comment
) {
}
