package ru.chousik.is.dto.message;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageDto(
        UUID id,
        UUID senderId,
        String body,
        OffsetDateTime sentAt,
        Boolean read
) {
}
