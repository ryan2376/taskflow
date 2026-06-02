package com.taskflow.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming JSON for POST /api/v1/auth/register.
 *
 * <p>A Java {@code record} is an immutable data carrier: this single line generates
 * a constructor, accessors (email(), password(), ...), equals/hashCode, and toString.
 * Ideal for DTOs.
 *
 * <p>The {@code jakarta.validation} annotations are enforced when the controller
 * marks the parameter {@code @Valid}. If any rule fails, Spring rejects the request
 * with 400 BEFORE our service code runs — we never validate by hand.
 */
public record RegisterRequest(

        @NotBlank(message = "email is required")
        @Email(message = "must be a valid email address")
        String email,

        // BCrypt only considers the first 72 bytes of input, so we cap there.
        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be 8-72 characters")
        String password,

        @NotBlank(message = "displayName is required")
        @Size(max = 100, message = "displayName must be at most 100 characters")
        String displayName
) {}
