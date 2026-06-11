package com.taskflow.api.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown for bad credentials at login → HTTP 401. */
public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
