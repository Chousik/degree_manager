package ru.chousik.is.dto.listing;

import java.math.BigDecimal;
import java.util.UUID;

public record ListingMapPoint(
        UUID id,
        String title,
        BigDecimal pricePerHour,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
