package com.portfolio.digitalatm.user;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String username,
        Set<String> roles,
        Instant createdAt
) {
}
