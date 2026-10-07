package com.shivanshu.personal_finance_manager.service;

import com.shivanshu.personal_finance_manager.dto.response.MonthlyReportResponse;
import com.shivanshu.personal_finance_manager.dto.response.YearlyReportResponse;

/**
 * Service contract for financial report generation operations.
 */
public interface ReportService {

    /**
     * Generates a monthly financial report for the current user.
     *
     * @param year  Report year
     * @param month Report month (1-12)
     * @return MonthlyReportResponse
     */
    MonthlyReportResponse getMonthlyReport(int year, int month);

    /**
     * Generates a yearly financial report for the current user.
     *
     * @param year Report year
     * @return YearlyReportResponse
     */
    YearlyReportResponse getYearlyReport(int year);
}
