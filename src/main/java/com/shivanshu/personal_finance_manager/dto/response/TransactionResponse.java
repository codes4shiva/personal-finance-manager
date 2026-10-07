package com.shivanshu.personal_finance_manager.dto.response;

import com.shivanshu.personal_finance_manager.entity.CategoryType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response payload representing a financial transaction.
 *
 * @param id          Transaction ID
 * @param amount      Monetary amount (scale 2)
 * @param date        Transaction date
 * @param category    Name of the associated category
 * @param description Transaction description
 * @param type        Transaction classification (INCOME or EXPENSE)
 */
public record TransactionResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        String category,
        String description,
        CategoryType type
) {
}
