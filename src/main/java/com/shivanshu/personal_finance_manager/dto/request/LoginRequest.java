package com.shivanshu.personal_finance_manager.dto.request;

/**
 * Request payload for user login.
 *
 * @param username User email address
 * @param password Raw password
 */
public record LoginRequest(
        String username,
        String password
) {
}
