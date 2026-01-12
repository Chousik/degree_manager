package ru.chousik.is.admin.dto.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SupportTicketResolutionRequest(
        @NotNull UUID adminId,
        @NotBlank String resolutionNotes
) {
}
