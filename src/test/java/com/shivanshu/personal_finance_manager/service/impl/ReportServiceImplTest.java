package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.response.MonthlyReportResponse;
import com.shivanshu.personal_finance_manager.dto.response.YearlyReportResponse;
import com.shivanshu.personal_finance_manager.security.CurrentUserProvider;
import com.shivanshu.personal_finance_manager.service.report.MonthlyReportGenerator;
import com.shivanshu.personal_finance_manager.service.report.YearlyReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReportServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private MonthlyReportGenerator monthlyReportGenerator;

    @Mock
    private YearlyReportGenerator yearlyReportGenerator;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(monthlyReportGenerator, yearlyReportGenerator, currentUserProvider);
    }

    @Test
    void getMonthlyReport_delegatesToMonthlyGenerator() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        MonthlyReportResponse expected = new MonthlyReportResponse(10, 2026, Map.of(), Map.of(), new BigDecimal("0.00"));
        when(monthlyReportGenerator.generate(eq(1L), any(MonthlyReportGenerator.MonthPeriod.class)))
                .thenReturn(expected);

        MonthlyReportResponse actual = reportService.getMonthlyReport(2026, 10);
        assertEquals(expected, actual);
    }

    @Test
    void getYearlyReport_delegatesToYearlyGenerator() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        YearlyReportResponse expected = new YearlyReportResponse(2026, Map.of(), Map.of(), new BigDecimal("0.00"));
        when(yearlyReportGenerator.generate(eq(1L), any(YearlyReportGenerator.YearPeriod.class)))
                .thenReturn(expected);

        YearlyReportResponse actual = reportService.getYearlyReport(2026);
        assertEquals(expected, actual);
    }
}
