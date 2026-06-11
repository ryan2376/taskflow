package com.taskflow.api.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown when the request is understood but invalid (e.g. a category that isn't yours) → HTTP 400. */
public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
