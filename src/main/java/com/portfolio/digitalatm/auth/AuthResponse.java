package com.portfolio.digitalatm.auth;

import com.portfolio.digitalatm.user.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {
}
