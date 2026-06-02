package com.taskflow.api.user;

import com.taskflow.api.user.dto.UserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoints for the currently authenticated user.
 *
 * <p>This is a PROTECTED controller — it isn't in SecurityConfig's public list, so a
 * request without a valid JWT never reaches it (the filter chain returns 401 first).
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/v1/users/me — return the caller's own profile.
     *
     * <p>{@code @AuthenticationPrincipal UUID userId} extracts the principal that
     * JwtAuthFilter placed in the SecurityContext: the user's id straight from the
     * verified token. No request parameter needed — identity comes from the JWT, so a
     * user can only ever ask for "me".
     */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UUID userId) {
        return UserResponse.from(userService.getById(userId));
    }
}
