package ru.chousik.is.dto.listing;

import java.util.UUID;

public record CategorySummaryDto(
        UUID id,
        String name
) {
}
