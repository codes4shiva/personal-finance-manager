package com.shivanshu.personal_finance_manager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for user registration.
 *
 * @param username    User email address
 * @param password    Raw password (8 to 72 characters)
 * @param fullName    User's full name
 * @param phoneNumber Contact phone number
 */
public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Email(message = "Username must be a valid email address")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^\\+?[0-9\\s\\-]{10,20}$",
                message = "Phone number must be 10-20 digits and may include an optional leading +, spaces, or hyphens"
        )
        String phoneNumber
) {
}
