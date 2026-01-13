package ru.chousik.is.dto.admin.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReportResolutionRequest(
        @NotNull UUID adminId,
        @NotBlank String status,
        String comment
) {
}
