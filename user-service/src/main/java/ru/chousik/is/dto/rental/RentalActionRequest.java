package ru.chousik.is.dto.rental;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RentalActionRequest(@NotNull UUID actorId) {
}
