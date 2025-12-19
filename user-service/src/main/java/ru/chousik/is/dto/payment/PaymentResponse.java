package ru.chousik.is.dto.payment;

import ru.chousik.is.entity.PaymentPurpose;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        PaymentPurpose purpose,
        String status,
        BigDecimal amount,
        String currency,
        String confirmationUrl,
        String externalId,
        OffsetDateTime paidAt,
        OffsetDateTime refundedAt
) {
}
