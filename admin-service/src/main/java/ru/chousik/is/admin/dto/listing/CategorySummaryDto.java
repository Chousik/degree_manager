package ru.chousik.is.admin.dto.listing;

import java.util.UUID;

public record CategorySummaryDto(
        UUID id,
        String name
) {
}
