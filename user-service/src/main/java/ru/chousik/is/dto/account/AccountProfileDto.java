package ru.chousik.is.dto.account;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountProfileDto(
        UUID id,
        String email,
        String username,
        String name,
        String surname,
        String lastName,
        String phone,
        String city,
        BigDecimal rating,
        String status,
        OffsetDateTime createdAt
) {
}
