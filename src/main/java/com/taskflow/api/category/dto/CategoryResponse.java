package com.taskflow.api.category.dto;

import java.util.UUID;

/**
 * Public representation of a category. We expose the id, name, and colour — but NOT
 * the owner, since a user only ever sees their own categories anyway.
 */
public record CategoryResponse(
        UUID id,
        String name,
        String color
) {}
