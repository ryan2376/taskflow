package com.taskflow.api.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a resource doesn't exist (or isn't the caller's) → HTTP 404. */
public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
