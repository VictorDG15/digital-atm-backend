package com.portfolio.digitalatm.card;

import java.time.Instant;

public record CardResponse(
        Long id,
        Long accountId,
        String accountNumber,
        String maskedNumber,
        CardStatus status,
        Instant createdAt
) {
}
