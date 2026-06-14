package com.portfolio.digitalatm.atm;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull Long sourceAccountId,
        @NotNull Long cardId,
        @Pattern(regexp = "^[0-9]{4}$", message = "El PIN debe tener 4 dígitos") String pin,
        @NotBlank @Size(max = 20) String targetAccountNumber,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank @Size(max = 120) String idempotencyKey,
        @Size(max = 255) String description
) {
}
