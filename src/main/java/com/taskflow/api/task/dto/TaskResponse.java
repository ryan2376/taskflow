package com.taskflow.api.task.dto;

import com.taskflow.api.category.dto.CategoryResponse;
import com.taskflow.api.task.entity.Priority;
import com.taskflow.api.task.entity.TaskStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Public representation of a task.
 *
 * <p>The category is embedded as a nested {@link CategoryResponse} (or null when the
 * task is uncategorised) — convenient for the frontend, which then doesn't need a
 * second request to show the category's name/colour.
 */
public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        Instant dueDate,
        Instant completedAt,
        CategoryResponse category,
        Instant createdAt,
        Instant updatedAt
) {}
