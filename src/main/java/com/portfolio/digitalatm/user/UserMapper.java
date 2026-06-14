package com.portfolio.digitalatm.user;

import java.util.stream.Collectors;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }
}
