package ru.chousik.is.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReviewRequest(
        @NotNull UUID authorId,
        @Min(1) @Max(5) short rating,
        @Size(max = 4000) String text
) {
}
