package com.shivanshu.personal_finance_manager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response payload representing a savings goal with dynamically computed progress metrics.
 *
 * @param id                 Goal ID
 * @param goalName           Goal name
 * @param targetAmount       Target monetary amount
 * @param targetDate         Target completion date
 * @param startDate          Start date of tracking
 * @param currentProgress    Current accumulated net savings
 * @param progressPercentage Progress percentage toward target (scale 2, floored at 0)
 * @param remainingAmount    Remaining monetary amount needed (floored at 0)
 */
public record GoalResponse(
        Long id,
        String goalName,
        BigDecimal targetAmount,
        LocalDate targetDate,
        LocalDate startDate,
        BigDecimal currentProgress,
        BigDecimal progressPercentage,
        BigDecimal remainingAmount
) {
}
