package ru.chousik.is.dto.moderation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FlagRequest(
        @NotNull UUID reporterId,
        @NotBlank String reason
) {
}
