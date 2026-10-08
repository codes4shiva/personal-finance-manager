package com.shivanshu.personal_finance_manager.service.report;

import com.shivanshu.personal_finance_manager.dto.response.YearlyReportResponse;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import com.shivanshu.personal_finance_manager.util.MoneyUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates yearly financial reports including per-category income and expense summaries.
 */
@Component
public class YearlyReportGenerator implements ReportGenerator<YearlyReportResponse, YearlyReportGenerator.YearPeriod> {

    public record YearPeriod(int year) {
    }

    private final TransactionRepository transactionRepository;

    public YearlyReportGenerator(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public YearlyReportResponse generate(Long userId, YearPeriod criteria) {
        int year = criteria.year();

        if (year < 1900 || year > 2100) {
            throw ApiException.badRequest("Year must be between 1900 and 2100");
        }

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

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

        return new YearlyReportResponse(year, totalIncome, totalExpenses, netSavings);
    }
}
