package com.taskflow.api.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a request clashes with existing data (e.g. an email already registered) → HTTP 409. */
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
