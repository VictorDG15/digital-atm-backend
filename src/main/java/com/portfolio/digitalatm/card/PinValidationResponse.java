package com.portfolio.digitalatm.card;

public record PinValidationResponse(
        boolean valid,
        String message
) {
}
