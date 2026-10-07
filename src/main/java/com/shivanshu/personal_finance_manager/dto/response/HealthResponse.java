package com.shivanshu.personal_finance_manager.dto.response;

/**
 * Response payload returned by the health check endpoint.
 *
 * @param status Health status string (e.g., "UP")
 */
public record HealthResponse(
        String status
) {
}
