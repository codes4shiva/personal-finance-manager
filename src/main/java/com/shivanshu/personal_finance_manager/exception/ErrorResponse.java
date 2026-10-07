package com.shivanshu.personal_finance_manager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Standard error response body returned by the API for all error scenarios.
 *
 * @param status  HTTP status code
 * @param error   HTTP status reason phrase
 * @param message Human-readable error description
 * @param details Optional list of detailed validation or contextual errors
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        List<String> details
) {

    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, null);
    }
}
