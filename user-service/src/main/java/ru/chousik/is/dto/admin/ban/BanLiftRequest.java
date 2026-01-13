package ru.chousik.is.dto.admin.ban;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BanLiftRequest(
        @NotNull UUID adminId,
        @NotBlank String status,
        String comment
) {
}
