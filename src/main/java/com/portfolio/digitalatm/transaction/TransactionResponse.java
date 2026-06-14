package com.portfolio.digitalatm.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        String transactionCode,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        String sourceAccountNumber,
        String targetAccountNumber,
        String idempotencyKey,
        String description,
        Instant createdAt
) {
}
