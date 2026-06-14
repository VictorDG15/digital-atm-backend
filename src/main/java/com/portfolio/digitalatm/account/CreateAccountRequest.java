package com.portfolio.digitalatm.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull AccountType type,
        @NotNull @DecimalMin(value = "0.00") BigDecimal initialBalance
) {
}
