package com.shivanshu.personal_finance_manager.service.progress;

import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.SavingsGoal;
import com.shivanshu.personal_finance_manager.entity.UserEntity;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetSavingsSinceStartCalculatorTest {

    @Mock
    private TransactionRepository transactionRepository;

    private Clock fixedClock;
    private NetSavingsSinceStartCalculator calculator;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneId.of("UTC"));
        calculator = new NetSavingsSinceStartCalculator(transactionRepository, fixedClock);
    }

    @Test
    @DisplayName("Calculate progress when startDate is after cutoffDate returns 0.00")
    void testStartDateAfterCutoff() {
        SavingsGoal goal = new SavingsGoal();
        goal.setStartDate(LocalDate.of(2026, 7, 1)); // After today (2026-06-01)
        goal.setTargetDate(LocalDate.of(2027, 1, 1));

        BigDecimal result = calculator.calculateProgress(goal);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    @DisplayName("Calculate progress when income > expense returns scaled net savings")
    void testNetSavingsPositive() {
        UserEntity user = new UserEntity("u@test.com", "pass", "User", "123");
        user.setId(1L);

        SavingsGoal goal = new SavingsGoal();
        goal.setUser(user);
        goal.setStartDate(LocalDate.of(2026, 1, 1));
        goal.setTargetDate(LocalDate.of(2026, 12, 31));

        LocalDate cutoff = LocalDate.of(2026, 6, 1);
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(eq(1L), eq(CategoryType.INCOME), eq(goal.getStartDate()), eq(cutoff)))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(eq(1L), eq(CategoryType.EXPENSE), eq(goal.getStartDate()), eq(cutoff)))
                .thenReturn(new BigDecimal("2000.00"));

        BigDecimal result = calculator.calculateProgress(goal);
        assertEquals(new BigDecimal("3000.00"), result);
    }

    @Test
    @DisplayName("Calculate progress when expense > income floors at 0.00")
    void testNetSavingsFlooredAtZero() {
        UserEntity user = new UserEntity("u@test.com", "pass", "User", "123");
        user.setId(1L);

        SavingsGoal goal = new SavingsGoal();
        goal.setUser(user);
        goal.setStartDate(LocalDate.of(2026, 1, 1));
        goal.setTargetDate(LocalDate.of(2026, 12, 31));

        LocalDate cutoff = LocalDate.of(2026, 6, 1);
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(eq(1L), eq(CategoryType.INCOME), eq(goal.getStartDate()), eq(cutoff)))
                .thenReturn(new BigDecimal("1000.00"));
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(eq(1L), eq(CategoryType.EXPENSE), eq(goal.getStartDate()), eq(cutoff)))
                .thenReturn(new BigDecimal("3000.00"));

        BigDecimal result = calculator.calculateProgress(goal);
        assertEquals(new BigDecimal("0.00"), result);
    }
}
