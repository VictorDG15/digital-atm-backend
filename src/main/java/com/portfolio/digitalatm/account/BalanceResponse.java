package com.portfolio.digitalatm.account;

import java.math.BigDecimal;

public record BalanceResponse(
        Long accountId,
        String accountNumber,
        AccountStatus status,
        BigDecimal balance
) {
}
