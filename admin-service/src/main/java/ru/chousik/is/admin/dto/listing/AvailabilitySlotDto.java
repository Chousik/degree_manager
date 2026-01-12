package ru.chousik.is.admin.dto.listing;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AvailabilitySlotDto(
        UUID id,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String note
) {
}
