package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.response.MonthlyReportResponse;
import com.shivanshu.personal_finance_manager.dto.response.YearlyReportResponse;
import com.shivanshu.personal_finance_manager.security.CurrentUserProvider;
import com.shivanshu.personal_finance_manager.service.ReportService;
import com.shivanshu.personal_finance_manager.service.report.MonthlyReportGenerator;
import com.shivanshu.personal_finance_manager.service.report.YearlyReportGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of ReportService delegating report calculations to specialized generators.
 */
@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final MonthlyReportGenerator monthlyReportGenerator;
    private final YearlyReportGenerator yearlyReportGenerator;
    private final CurrentUserProvider currentUserProvider;

    public ReportServiceImpl(
            MonthlyReportGenerator monthlyReportGenerator,
            YearlyReportGenerator yearlyReportGenerator,
            CurrentUserProvider currentUserProvider
    ) {
        this.monthlyReportGenerator = monthlyReportGenerator;
        this.yearlyReportGenerator = yearlyReportGenerator;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        Long userId = currentUserProvider.getCurrentUserId();
        return monthlyReportGenerator.generate(userId, new MonthlyReportGenerator.MonthPeriod(year, month));
    }

    @Override
    public YearlyReportResponse getYearlyReport(int year) {
        Long userId = currentUserProvider.getCurrentUserId();
        return yearlyReportGenerator.generate(userId, new YearlyReportGenerator.YearPeriod(year));
    }
}
