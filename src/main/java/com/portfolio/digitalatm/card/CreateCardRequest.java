package com.portfolio.digitalatm.card;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateCardRequest(
        @NotNull Long accountId,
        @Pattern(regexp = "^[0-9]{4}$", message = "El PIN debe tener 4 dígitos") String pin
) {
}
