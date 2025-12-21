package ru.chousik.is.dto.account;

import jakarta.validation.constraints.NotBlank;

public record CityUpdateRequest(@NotBlank String city) {
}
