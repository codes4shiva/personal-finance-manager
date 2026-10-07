package com.shivanshu.personal_finance_manager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload for creating a transaction.
 *
 * @param amount      Monetary amount greater than 0
 * @param date        Transaction date (YYYY-MM-DD, not in future)
 * @param category    Category name
 * @param description Optional description (max 500 characters)
 */
public record CreateTransactionRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotBlank(message = "Category is required")
        String category,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
