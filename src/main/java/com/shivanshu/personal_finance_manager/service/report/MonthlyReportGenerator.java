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

        return new MonthlyReportResponse(month, year, totalIncome, totalExpenses, netSavings);
    }
}
