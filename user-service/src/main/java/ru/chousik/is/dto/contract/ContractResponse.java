package ru.chousik.is.dto.contract;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        UUID rentalId,
        String status,
        OffsetDateTime signedAt,
        String fileUrl,
        String signatureHash
) {
}
