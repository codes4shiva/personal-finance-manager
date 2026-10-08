package com.shivanshu.personal_finance_manager.service.report;

import com.shivanshu.personal_finance_manager.dto.response.MonthlyReportResponse;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import com.shivanshu.personal_finance_manager.util.MoneyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates monthly financial reports including per-category income and expense summaries.
 */
@Component
public class MonthlyReportGenerator implements ReportGenerator<MonthlyReportResponse, MonthlyReportGenerator.MonthPeriod> {

    public record MonthPeriod(int year, int month) {
    }

    private final TransactionRepository transactionRepository;

    public MonthlyReportGenerator(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public MonthlyReportResponse generate(Long userId, MonthPeriod criteria) {
        int year = criteria.year();
        int month = criteria.month();

        if (month < 1 || month > 12) {
            throw ApiException.badRequest("Month must be between 1 and 12");
        }
        if (year < 1900 || year > 2100) {
            throw ApiException.badRequest("Year must be between 1900 and 2100");
        }

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<TransactionRepository.CategorySummaryProjection> summaries =
                transactionRepository.sumAmountByCategoryBetweenDates(userId, startDate, endDate);

        Map<String, BigDecimal> totalIncome = new LinkedHashMap<>();
        Map<String, BigDecimal> totalExpenses = new LinkedHashMap<>();
        BigDecimal incomeSum = BigDecimal.ZERO;
        BigDecimal expenseSum = BigDecimal.ZERO;

        for (TransactionRepository.CategorySummaryProjection row : summaries) {
            BigDecimal amount = MoneyUtils.scale(row.getTotalAmount());
            if (row.getCategoryType() == CategoryType.INCOME) {
                totalIncome.put(row.getCategoryName(), amount);
                incomeSum = incomeSum.add(amount);
            } else if (row.getCategoryType() == CategoryType.EXPENSE) {
                totalExpenses.put(row.getCategoryName(), amount);
                expenseSum = expenseSum.add(amount);
            }
        }

        BigDecimal netSavings = MoneyUtils.scale(incomeSum.subtract(expenseSum));

        return new MonthlyReportResponse(month, year, totalIncome, totalExpenses, netSavings);
    }
}
