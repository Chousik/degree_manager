package ru.chousik.is.admin.dto.support;

import ru.chousik.is.admin.model.SupportTicketStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SupportTicketResponse(
        UUID id,
        UUID requesterId,
        UUID rentalId,
        SupportTicketStatus status,
        String subject,
        String message,
        OffsetDateTime createdAt,
        OffsetDateTime resolvedAt,
        String resolutionNotes
) {
}
