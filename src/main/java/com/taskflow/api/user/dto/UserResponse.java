package com.taskflow.api.user.dto;

import com.taskflow.api.user.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Public representation of a user. Deliberately omits passwordHash — there is no
 * field for it, so it can never be serialised into a response.
 */
public record UserResponse(
        UUID id,
        String email,
        String displayName,
        Instant createdAt
) {
    /** Map a User entity to its safe public view. */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCreatedAt()
        );
    }
}
