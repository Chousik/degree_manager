package ru.chousik.is.dto.admin.ban;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BanCreateRequest(
        @NotNull UUID adminId,
        @NotNull UUID bannedUserId,
        String banReason,
        @NotBlank String banType,
        OffsetDateTime banDuration,
        @NotBlank String status
) {
}
