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

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        Map<String, BigDecimal> totalIncome = new LinkedHashMap<>();
        Map<String, BigDecimal> totalExpenses = new LinkedHashMap<>();

        for (Transaction tx : transactions) {
            String categoryName = tx.getCategory().getName();
            BigDecimal amount = MoneyUtils.scale(tx.getAmount());

            if (tx.getType() == CategoryType.INCOME) {
                totalIncome.merge(categoryName, amount, BigDecimal::add);
            } else if (tx.getType() == CategoryType.EXPENSE) {
                totalExpenses.merge(categoryName, amount, BigDecimal::add);
            }
        }

        BigDecimal incomeSum = totalIncome.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenseSum = totalExpenses.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSavings = MoneyUtils.scale(incomeSum.subtract(expenseSum));

        return new YearlyReportResponse(year, totalIncome, totalExpenses, netSavings);
    }
}
