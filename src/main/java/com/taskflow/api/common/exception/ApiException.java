package com.taskflow.api.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for our own "expected" errors — the ones we deliberately throw from the
 * service layer (not-found, bad-request, etc.).
 *
 * <p>Each subclass bakes in the {@link HttpStatus} it represents, so the
 * GlobalExceptionHandler can read {@code getStatus()} and respond correctly without a
 * separate handler method per type. Extends {@link RuntimeException} (an "unchecked"
 * exception) so we don't have to declare {@code throws} on every method.
 *
 * <p>Abstract because you never throw a generic ApiException — you throw a specific one
 * like {@link NotFoundException}.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
