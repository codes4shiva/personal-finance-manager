package com.shivanshu.personal_finance_manager.service.progress;

import com.shivanshu.personal_finance_manager.entity.SavingsGoal;

import java.math.BigDecimal;

/**
 * Extension point interface for calculating current progress toward a savings goal.
 */
public interface GoalProgressCalculator {

    /**
     * Computes the current monetary progress toward the specified goal.
     *
     * @param goal The savings goal entity
     * @return Current progress amount
     */
    BigDecimal calculateProgress(SavingsGoal goal);
}
