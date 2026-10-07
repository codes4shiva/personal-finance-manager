package com.shivanshu.personal_finance_manager.service.report;

import com.shivanshu.personal_finance_manager.dto.response.MonthlyReportResponse;
import com.shivanshu.personal_finance_manager.entity.Category;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MonthlyReportGenerator.
 */
@ExtendWith(MockitoExtension.class)
class MonthlyReportGeneratorTest {

    @Mock
    private TransactionRepository transactionRepository;

    private MonthlyReportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new MonthlyReportGenerator(transactionRepository);
    }

    @Test
    void generate_validMonthAndYear_aggregatesCorrectly() {
        Long userId = 1L;
        int year = 2026;
        int month = 10;

        Category salaryCat = new Category("Salary", CategoryType.INCOME, false, null);
        Category foodCat = new Category("Food", CategoryType.EXPENSE, false, null);

        Transaction tx1 = new Transaction();
        tx1.setCategory(salaryCat);
        tx1.setAmount(new BigDecimal("10000.00"));

        Transaction tx2 = new Transaction();
        tx2.setCategory(foodCat);
        tx2.setAmount(new BigDecimal("1500.00"));

        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);
        when(transactionRepository.findByUserIdAndDateBetween(userId, start, end))
                .thenReturn(List.of(tx1, tx2));

        MonthlyReportResponse report = generator.generate(userId, new MonthlyReportGenerator.MonthPeriod(year, month));

        assertEquals(10, report.month());
        assertEquals(2026, report.year());
        assertEquals(new BigDecimal("10000.00"), report.totalIncome().get("Salary"));
        assertEquals(new BigDecimal("1500.00"), report.totalExpenses().get("Food"));
        assertEquals(new BigDecimal("8500.00"), report.netSavings());
    }

    @Test
    void generate_whenNoTransactions_returnsEmptyMapsAndZeroNetSavings() {
        Long userId = 1L;
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);
        when(transactionRepository.findByUserIdAndDateBetween(userId, start, end))
                .thenReturn(List.of());

        MonthlyReportResponse report = generator.generate(userId, new MonthlyReportGenerator.MonthPeriod(2026, 5));

        assertTrue(report.totalIncome().isEmpty());
        assertTrue(report.totalExpenses().isEmpty());
        assertEquals(new BigDecimal("0.00"), report.netSavings());
    }

    @Test
    void generate_invalidMonth_throwsBadRequest() {
        ApiException ex1 = assertThrows(ApiException.class,
                () -> generator.generate(1L, new MonthlyReportGenerator.MonthPeriod(2026, 0)));
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatus());

        ApiException ex2 = assertThrows(ApiException.class,
                () -> generator.generate(1L, new MonthlyReportGenerator.MonthPeriod(2026, 13)));
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatus());
    }

    @Test
    void generate_invalidYear_throwsBadRequest() {
        ApiException ex = assertThrows(ApiException.class,
                () -> generator.generate(1L, new MonthlyReportGenerator.MonthPeriod(1800, 5)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
