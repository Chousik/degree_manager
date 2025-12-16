package ru.chousik.is.dto.listing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ListingSearchRequest(
        String text,
        UUID categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        OffsetDateTime availableFrom,
        OffsetDateTime availableTo,
        BigDecimal minLatitude,
        BigDecimal maxLatitude,
        BigDecimal minLongitude,
        BigDecimal maxLongitude
) {
}
