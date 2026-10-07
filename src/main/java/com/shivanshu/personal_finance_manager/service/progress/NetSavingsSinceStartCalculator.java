package com.shivanshu.personal_finance_manager.service.progress;

import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.SavingsGoal;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import com.shivanshu.personal_finance_manager.util.MoneyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Calculates savings goal progress based on net savings (income minus expenses)
 * accumulated from the goal's start date onwards.
 */
@Component
public class NetSavingsSinceStartCalculator implements GoalProgressCalculator {

    private final TransactionRepository transactionRepository;

    public NetSavingsSinceStartCalculator(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public BigDecimal calculateProgress(SavingsGoal goal) {
        Long userId = goal.getUser().getId();
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateOnOrAfter(
                userId,
                CategoryType.INCOME,
                goal.getStartDate()
        );
        BigDecimal totalExpenses = transactionRepository.sumAmountByUserIdAndTypeAndDateOnOrAfter(
                userId,
                CategoryType.EXPENSE,
                goal.getStartDate()
        );

        BigDecimal income = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        BigDecimal expenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;

        return MoneyUtils.scale(income.subtract(expenses));
    }
}
