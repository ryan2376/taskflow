package com.taskflow.api.task.dto;

import com.taskflow.api.task.entity.Priority;
import com.taskflow.api.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Incoming JSON for creating/updating a task.
 *
 * <p>Jackson maps the JSON strings "TODO"/"HIGH" straight onto our enums; an invalid
 * value (e.g. "URGENT") is rejected as a 400 before our code runs. {@code dueDate} is
 * an ISO-8601 instant like "2026-07-01T09:00:00Z".
 *
 * <p>Optional fields (status, priority, dueDate, categoryId) may be null. The service
 * supplies sensible defaults (status→TODO, priority→MEDIUM) and validates that any
 * categoryId actually belongs to the caller.
 */
public record TaskRequest(

        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title must be at most 255 characters")
        String title,

        String description,

        TaskStatus status,     // optional; defaults to TODO when omitted

        Priority priority,     // optional; defaults to MEDIUM when omitted

        Instant dueDate,       // optional

        UUID categoryId        // optional; if present, must be one of the caller's categories
) {}
