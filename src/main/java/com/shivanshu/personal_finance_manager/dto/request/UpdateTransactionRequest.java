package com.shivanshu.personal_finance_manager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload for updating an existing transaction.
 * Note: Transaction date is immutable and cannot be changed.
 *
 * @param amount      Updated monetary amount (must be > 0 if provided)
 * @param description Updated description (max 500 characters)
 * @param category    Updated category name
 * @param date        Optional date field (if provided and different from existing date, results in 400 Bad Request)
 */
public record UpdateTransactionRequest(
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        String category,

        LocalDate date
) {
}
