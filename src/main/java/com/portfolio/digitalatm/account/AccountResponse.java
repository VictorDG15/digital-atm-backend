package com.portfolio.digitalatm.account;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        Long id,
        String accountNumber,
        AccountType type,
        AccountStatus status,
        BigDecimal balance,
        Long ownerId,
        String ownerUsername,
        Instant createdAt
) {
}
