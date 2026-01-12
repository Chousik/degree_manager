package ru.chousik.is.admin.dto.report;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportSummaryDto(
        UUID id,
        UUID reporterId,
        String status,
        String targetType,
        UUID targetId,
        String reasonBody,
        String moderatorComment,
        OffsetDateTime createdAt,
        UUID resolvedById
) {
}
