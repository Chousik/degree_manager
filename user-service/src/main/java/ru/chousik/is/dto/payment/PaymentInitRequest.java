package ru.chousik.is.dto.payment;

import jakarta.validation.constraints.NotNull;
import ru.chousik.is.entity.PaymentPurpose;

import java.util.UUID;

public record PaymentInitRequest(
        @NotNull UUID actorId,
        @NotNull PaymentPurpose purpose,
        String returnUrl
) {
}
