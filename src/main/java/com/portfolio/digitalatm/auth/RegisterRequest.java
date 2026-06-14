package com.portfolio.digitalatm.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Size(min = 4, max = 60) String username,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 6, max = 80) String password
) {
}
