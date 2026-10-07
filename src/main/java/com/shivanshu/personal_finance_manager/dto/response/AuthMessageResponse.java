package com.shivanshu.personal_finance_manager.dto.response;

/**
 * Standard response payload containing a message string.
 *
 * @param message Informational or status message
 */
public record AuthMessageResponse(
        String message
) {
}
