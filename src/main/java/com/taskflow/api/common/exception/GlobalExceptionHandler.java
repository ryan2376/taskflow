package com.taskflow.api.common.exception;

import com.taskflow.api.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The single, application-wide error handler.
 *
 * <p>{@code @RestControllerAdvice} makes this class a global "catch block": whenever any
 * controller (or the code it calls) throws, Spring looks here for an {@code @ExceptionHandler}
 * whose type matches, runs it, and serialises the returned {@link ErrorResponse} to JSON.
 * The result is that EVERY error in the API shares one predictable shape.
 *
 * <p>Note: authentication failures for missing/expired tokens are handled earlier, by
 * Spring Security's entry point in SecurityConfig (they never reach a controller), so they
 * aren't routed here. Bad-credentials at login DO reach here, via {@link UnauthorizedException}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Our own domain exceptions. Each carries the right {@link HttpStatus}, so one handler
     * covers NotFound (404), BadRequest (400), Conflict (409), Unauthorized (401), etc.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getMessage(), request);
    }

    /**
     * A {@code @Valid} request body broke its rules. We collect every field's message into
     * {@code details} so the frontend can show errors next to the right inputs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), details);
        return ResponseEntity.badRequest().body(body);
    }

    /** A query/path parameter couldn't be converted, e.g. {@code ?status=URGENT} or a malformed UUID. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'", request);
    }

    /** The JSON body was malformed or, e.g., contained an invalid enum value. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body", request);
    }

    /**
     * The safety net. Anything we didn't anticipate becomes a 500 — but we LOG the real
     * cause for ourselves while returning only a generic message to the client (never leak
     * internals or stack traces over the wire).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    /** Shared builder so every response is assembled the same way. */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message, request.getRequestURI()));
    }
}
