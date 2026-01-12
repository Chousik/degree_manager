package ru.chousik.is.admin.dto.support;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SupportTicketStatusRequest(
        @NotNull UUID adminId
) {
}
