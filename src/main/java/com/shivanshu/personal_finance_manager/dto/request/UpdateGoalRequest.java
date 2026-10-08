package com.shivanshu.personal_finance_manager.dto.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload for updating a savings goal.
 * At least one field (targetAmount or targetDate) must be provided.
 *
 * @param targetAmount Optional updated target amount (must be > 0 if provided)
 * @param targetDate   Optional updated target date (must be in the future)
 */
public record UpdateGoalRequest(
        String goalName,

        @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
        BigDecimal targetAmount,

        LocalDate targetDate,

        LocalDate startDate
) {
}
