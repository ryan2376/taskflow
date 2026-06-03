package com.taskflow.api.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming JSON for creating or updating a category (POST/PUT /api/v1/categories).
 *
 * <p>Note there is NO owner field — ownership is never client-supplied. The owner is
 * always the authenticated user, taken from the JWT in the service layer. Accepting an
 * owner from the request would be a serious security hole (a user could create data
 * "as" someone else).
 */
public record CategoryRequest(

        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        // Optional display colour (e.g. "#3B82F6"). Kept free-form/short.
        @Size(max = 20, message = "color must be at most 20 characters")
        String color
) {}
