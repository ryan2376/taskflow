package com.taskflow.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Incoming JSON for POST /api/v1/auth/login.
 *
 * <p>Note we DON'T apply size/strength rules to the password here — on login we only
 * need it present. The credentials are checked against the stored hash; rejecting a
 * wrong password is a 401, not a 400 validation error.
 */
public record LoginRequest(

        @NotBlank(message = "email is required")
        @Email(message = "must be a valid email address")
        String email,

        @NotBlank(message = "password is required")
        String password
) {}
