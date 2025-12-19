package ru.chousik.is.dto.rental;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RentalCreateRequest(
        @NotNull UUID listingId,
        @NotNull UUID lesseeId,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt,
        @Positive(message = "Deposit must be positive") BigDecimal depositAmount
) {
}
