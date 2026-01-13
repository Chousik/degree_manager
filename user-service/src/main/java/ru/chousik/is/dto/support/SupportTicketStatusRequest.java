package ru.chousik.is.dto.support;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SupportTicketStatusRequest(
        @NotNull UUID adminId
) {
}
