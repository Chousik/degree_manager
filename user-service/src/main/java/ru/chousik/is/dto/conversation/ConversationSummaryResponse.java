package ru.chousik.is.dto.conversation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationSummaryResponse(
        UUID conversationId,
        UUID listingId,
        String listingTitle,
        String listingPhotoUrl,
        UUID counterpartyId,
        String counterpartyName,
        String counterpartyUsername,
        String lastMessagePreview,
        OffsetDateTime lastMessageAt
) {
}
