package com.taskflow.api.auth.controller;

import com.taskflow.api.auth.dto.AuthResponse;
import com.taskflow.api.auth.dto.LoginRequest;
import com.taskflow.api.auth.dto.RegisterRequest;
import com.taskflow.api.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication endpoints. These paths are permitted (no token required) by
 * SecurityConfig's PUBLIC_PATHS.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Create an account. Returns 201 Created with the new user + a token.
     * {@code @Valid} enforces the RegisterRequest rules; a violation short-circuits
     * to a 400 before this method body runs.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Exchange credentials for a token. Returns 200 OK. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
