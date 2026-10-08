package com.shivanshu.personal_finance_manager.mapper;

import com.shivanshu.personal_finance_manager.dto.response.GoalResponse;
import com.shivanshu.personal_finance_manager.entity.SavingsGoal;
import com.shivanshu.personal_finance_manager.util.MoneyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mapper converting SavingsGoal entities and calculated progress values into GoalResponse DTOs.
 */
@Component
public class GoalMapper {

    /**
     * Converts a SavingsGoal and its calculated progress into a GoalResponse DTO.
     *
     * @param goal     The savings goal entity
     * @param progress The computed net savings progress
     * @return GoalResponse containing goal attributes and progress metrics
     */
    public GoalResponse toResponse(SavingsGoal goal, BigDecimal progress) {
        if (goal == null) {
            return null;
        }

        BigDecimal scaledProgress = MoneyUtils.scale(progress);
        BigDecimal target = MoneyUtils.scale(goal.getTargetAmount());

        BigDecimal percentage;
        if (target.compareTo(BigDecimal.ZERO) > 0) {
            percentage = scaledProgress
                    .multiply(BigDecimal.valueOf(100))
                    .divide(target, MoneyUtils.SCALE, RoundingMode.HALF_UP);
            if (percentage.compareTo(BigDecimal.ZERO) < 0) {
                percentage = MoneyUtils.ZERO;
            }
        } else {
            percentage = MoneyUtils.ZERO;
        }

        BigDecimal remaining = target.subtract(scaledProgress);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = MoneyUtils.ZERO;
        }
        remaining = MoneyUtils.scale(remaining);

        return new GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                target,
                goal.getTargetDate(),
                goal.getStartDate(),
                scaledProgress,
                percentage,
                remaining
        );
    }
}
