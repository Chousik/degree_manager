package ru.chousik.is.dto.notification;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String body,
        Boolean isRead,
        OffsetDateTime createdAt
) {
}
