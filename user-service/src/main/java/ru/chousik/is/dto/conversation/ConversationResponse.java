package ru.chousik.is.dto.conversation;

import java.util.UUID;

public record ConversationResponse(
        UUID conversationId,
        UUID listingId
) {
}
