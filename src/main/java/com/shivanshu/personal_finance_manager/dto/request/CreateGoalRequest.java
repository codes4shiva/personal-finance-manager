package com.shivanshu.personal_finance_manager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload for creating a savings goal.
 *
 * @param goalName     Goal name
 * @param targetAmount Monetary target amount greater than 0
 * @param targetDate   Target date in the future
 * @param startDate    Optional start date (defaults to today if omitted)
 */
public record CreateGoalRequest(
        @NotBlank(message = "Goal name is required")
        String goalName,

        @NotNull(message = "Target amount is required")
        @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
        BigDecimal targetAmount,

        @NotNull(message = "Target date is required")
        LocalDate targetDate,

        LocalDate startDate
) {
}
