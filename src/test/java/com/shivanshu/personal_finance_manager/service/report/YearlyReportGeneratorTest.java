package com.shivanshu.personal_finance_manager.service.report;

import com.shivanshu.personal_finance_manager.dto.response.YearlyReportResponse;
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
 * Unit tests for YearlyReportGenerator.
 */
@ExtendWith(MockitoExtension.class)
class YearlyReportGeneratorTest {

    @Mock
    private TransactionRepository transactionRepository;

    private YearlyReportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new YearlyReportGenerator(transactionRepository);
    }

    @Test
    void generate_validYear_aggregatesCorrectly() {
        Long userId = 1L;
        int year = 2026;

        Category salaryCat = new Category("Salary", CategoryType.INCOME, false, null);
        Category rentCat = new Category("Rent", CategoryType.EXPENSE, false, null);

        Transaction tx1 = new Transaction();
        tx1.setCategory(salaryCat);
        tx1.setAmount(new BigDecimal("120000.00"));

        Transaction tx2 = new Transaction();
        tx2.setCategory(rentCat);
        tx2.setAmount(new BigDecimal("30000.00"));

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        when(transactionRepository.findByUserIdAndDateBetween(userId, start, end))
                .thenReturn(List.of(tx1, tx2));

        YearlyReportResponse report = generator.generate(userId, new YearlyReportGenerator.YearPeriod(year));

        assertEquals(2026, report.year());
        assertEquals(new BigDecimal("120000.00"), report.totalIncome().get("Salary"));
        assertEquals(new BigDecimal("30000.00"), report.totalExpenses().get("Rent"));
        assertEquals(new BigDecimal("90000.00"), report.netSavings());
    }

    @Test
    void generate_whenNoTransactions_returnsEmptyMapsAndZero() {
        Long userId = 1L;
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        when(transactionRepository.findByUserIdAndDateBetween(userId, start, end))
                .thenReturn(List.of());

        YearlyReportResponse report = generator.generate(userId, new YearlyReportGenerator.YearPeriod(2026));

        assertTrue(report.totalIncome().isEmpty());
        assertTrue(report.totalExpenses().isEmpty());
        assertEquals(new BigDecimal("0.00"), report.netSavings());
    }

    @Test
    void generate_invalidYear_throwsBadRequest() {
        ApiException ex = assertThrows(ApiException.class,
                () -> generator.generate(1L, new YearlyReportGenerator.YearPeriod(1899)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
