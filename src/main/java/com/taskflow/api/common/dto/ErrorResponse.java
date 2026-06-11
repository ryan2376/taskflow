package com.taskflow.api.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

/**
 * The ONE error shape every failed request returns, so clients (our Next.js frontend)
 * can always parse errors the same way.
 *
 * <p>{@code @JsonInclude(NON_NULL)} means any field that is null is simply omitted from
 * the JSON — so {@code details} only appears for validation errors, and is absent
 * otherwise (matching the "details?" optional in the brief).
 *
 * @param timestamp when the error happened (UTC)
 * @param status    the numeric HTTP status, e.g. 404
 * @param error     the status reason phrase, e.g. "Not Found"
 * @param message   a human-readable explanation
 * @param path      the request path that failed, e.g. /api/v1/tasks/123
 * @param details   field-by-field messages, present ONLY for validation failures
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> details
) {
    /** Build a plain error (no field details). */
    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path, null);
    }

    /** Build a validation error that carries per-field messages. */
    public static ErrorResponse of(HttpStatus status, String message, String path, Map<String, String> details) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path, details);
    }
}
