package ru.chousik.is.dto.moderation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ModerationResolutionRequest(
        @NotNull UUID adminId,
        @NotBlank String action,
        String comment
) {
}
