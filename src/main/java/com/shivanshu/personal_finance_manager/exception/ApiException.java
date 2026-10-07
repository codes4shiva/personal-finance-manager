package com.shivanshu.personal_finance_manager.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Custom application exception representing known API error scenarios
 * with an associated HTTP status code and optional error details.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final List<String> details;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.details = null;
    }

    public ApiException(HttpStatus status, String message, List<String> details) {
        super(message);
        this.status = status;
        this.details = details;
    }

    /**
     * Creates a 400 Bad Request exception.
     */
    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Creates a 400 Bad Request exception with validation details.
     */
    public static ApiException badRequest(String message, List<String> details) {
        return new ApiException(HttpStatus.BAD_REQUEST, message, details);
    }

    /**
     * Creates a 401 Unauthorized exception.
     */
    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * Creates a 403 Forbidden exception.
     */
    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }

    /**
     * Creates a 404 Not Found exception.
     */
    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    /**
     * Creates a 409 Conflict exception.
     */
    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getDetails() {
        return details;
    }
}
