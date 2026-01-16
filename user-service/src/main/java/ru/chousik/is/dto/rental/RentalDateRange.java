package ru.chousik.is.dto.rental;

import java.time.OffsetDateTime;

public record RentalDateRange(
        OffsetDateTime startAt,
        OffsetDateTime endAt
) {
}
