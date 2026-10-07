package com.shivanshu.personal_finance_manager.dto.response;

/**
 * Response payload returned upon successful user registration.
 *
 * @param message Success message
 * @param userId  ID of the registered user
 */
public record RegisterResponse(
        String message,
        Long userId
) {
}
