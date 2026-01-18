package ru.chousik.is.dto.realtime;

import ru.chousik.is.entity.RentalStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RentalRealtimePayload(
        String event,
        UUID rentalId,
        UUID listingId,
        String listingTitle,
        RentalStatus status,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        OffsetDateTime updatedAt
) {
}
