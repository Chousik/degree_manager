package ru.chousik.is.dto.admin.review;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReviewHideRequest(
        @NotNull UUID adminId,
        UUID reportId,
        String comment
) {
}
