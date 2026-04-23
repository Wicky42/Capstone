package org.example.backend.user.dto;

import org.example.backend.user.model.User;

public record UserResponse(
        String id,
        String name,
        String email,
        User.Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}

