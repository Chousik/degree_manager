package ru.chousik.is.dto.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SupportTicketRequest(
        @NotNull UUID requesterId,
        UUID rentalId,
        @NotBlank String subject,
        @NotBlank String message
) {
}
