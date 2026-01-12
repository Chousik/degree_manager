package ru.chousik.is.admin.dto.ban;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BanResponse(
        UUID id,
        UUID bannedUserId,
        UUID adminUserId,
        String banReason,
        String banType,
        OffsetDateTime banDuration,
        String status,
        OffsetDateTime createdAt
) {
}
