package ru.chousik.is.dto.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        int status,
        String error,
        String message,
        Instant timestamp,
        List<FieldValidationError> fieldErrors
) {
}
