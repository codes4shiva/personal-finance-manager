package com.shivanshu.personal_finance_manager.dto.response;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response payload representing a monthly financial report.
 *
 * @param month         Month of the report (1-12)
 * @param year          Year of the report
 * @param totalIncome   Map of income category names to aggregate amounts
 * @param totalExpenses Map of expense category names to aggregate amounts
 * @param netSavings    Calculated net savings (total income minus total expenses)
 */
public record MonthlyReportResponse(
        int month,
        int year,
        Map<String, BigDecimal> totalIncome,
        Map<String, BigDecimal> totalExpenses,
        BigDecimal netSavings
) {
}
