package com.shivanshu.personal_finance_manager.service.progress;

import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.SavingsGoal;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import com.shivanshu.personal_finance_manager.util.MoneyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Calculates savings goal progress based on net savings (income minus expenses)
 * accumulated between the goal's start date and min(today, targetDate), floored at 0.
 */
@Component
public class NetSavingsSinceStartCalculator implements GoalProgressCalculator {

    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public NetSavingsSinceStartCalculator(
            TransactionRepository transactionRepository,
            Clock clock
    ) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Override
    public BigDecimal calculateProgress(SavingsGoal goal) {
        LocalDate today = LocalDate.now(clock);
        LocalDate targetDate = goal.getTargetDate();
        LocalDate cutoffDate = today.isBefore(targetDate) ? today : targetDate;

        if (goal.getStartDate().isAfter(cutoffDate)) {
            return MoneyUtils.ZERO;
        }

        Long userId = goal.getUser().getId();
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId,
                CategoryType.INCOME,
                goal.getStartDate(),
                cutoffDate
        );
        BigDecimal totalExpenses = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId,
                CategoryType.EXPENSE,
                goal.getStartDate(),
                cutoffDate
        );

        BigDecimal income = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        BigDecimal expenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;

        BigDecimal netSavings = income.subtract(expenses);
        if (netSavings.compareTo(BigDecimal.ZERO) < 0) {
            netSavings = BigDecimal.ZERO;
        }

        return MoneyUtils.scale(netSavings);
    }
}
