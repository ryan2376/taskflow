package com.taskflow.api.auth.dto;

import com.taskflow.api.user.dto.UserResponse;

/**
 * Response for register/login: the signed JWT plus the user's public info.
 *
 * <p>{@code tokenType} is "Bearer" — a hint to clients that they should send the
 * token as {@code Authorization: Bearer <token>} on subsequent requests.
 */
public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) {
    public static AuthResponse bearer(String token, UserResponse user) {
        return new AuthResponse(token, "Bearer", user);
    }
}
