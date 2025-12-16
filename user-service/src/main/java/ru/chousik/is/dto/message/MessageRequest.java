package ru.chousik.is.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MessageRequest(
        @NotNull UUID senderId,
        @NotBlank @Size(max = 4000) String body
) {
}
