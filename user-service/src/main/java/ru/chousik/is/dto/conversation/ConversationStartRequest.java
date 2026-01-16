package ru.chousik.is.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConversationStartRequest(
        @NotNull UUID senderId,
        @NotBlank String body
) {
}
