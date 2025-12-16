package ru.chousik.is.dto.support;

import ru.chousik.is.entity.SupportTicketStatus;

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
