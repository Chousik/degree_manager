package ru.chousik.is.dto.realtime;

import ru.chousik.is.dto.message.MessageDto;

import java.util.UUID;

public record MessageRealtimePayload(
        String event,
        UUID conversationId,
        UUID rentalId,
        MessageDto message
) {
}
