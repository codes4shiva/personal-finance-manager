package com.shivanshu.personal_finance_manager.service.progress;

import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.SavingsGoal;
import com.shivanshu.personal_finance_manager.entity.User;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for NetSavingsSinceStartCalculator.
 */
@ExtendWith(MockitoExtension.class)
class NetSavingsSinceStartCalculatorTest {

    @Mock
    private TransactionRepository transactionRepository;

    private NetSavingsSinceStartCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new NetSavingsSinceStartCalculator(transactionRepository);
    }

    @Test
    void calculateProgress_computesIncomeMinusExpenses() {
        User user = new User();
        user.setId(10L);

        LocalDate startDate = LocalDate.of(2026, 1, 1);
        SavingsGoal goal = new SavingsGoal();
        goal.setUser(user);
        goal.setStartDate(startDate);

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateOnOrAfter(10L, CategoryType.INCOME, startDate))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateOnOrAfter(10L, CategoryType.EXPENSE, startDate))
                .thenReturn(new BigDecimal("2300.50"));

        BigDecimal progress = calculator.calculateProgress(goal);
        assertEquals(new BigDecimal("2699.50"), progress);
    }

    @Test
    void calculateProgress_whenNullSums_treatsAsZero() {
        User user = new User();
        user.setId(10L);

        LocalDate startDate = LocalDate.of(2026, 1, 1);
        SavingsGoal goal = new SavingsGoal();
        goal.setUser(user);
        goal.setStartDate(startDate);

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateOnOrAfter(10L, CategoryType.INCOME, startDate))
                .thenReturn(null);
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateOnOrAfter(10L, CategoryType.EXPENSE, startDate))
                .thenReturn(null);

        BigDecimal progress = calculator.calculateProgress(goal);
        assertEquals(new BigDecimal("0.00"), progress);
    }
}
