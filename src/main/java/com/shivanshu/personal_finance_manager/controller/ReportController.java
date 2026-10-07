package com.shivanshu.personal_finance_manager.controller;

import com.shivanshu.personal_finance_manager.dto.response.MonthlyReportResponse;
import com.shivanshu.personal_finance_manager.dto.response.YearlyReportResponse;
import com.shivanshu.personal_finance_manager.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing REST endpoints for financial reports.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Retrieves a monthly income and expense report.
     *
     * @param year  Report year
     * @param month Report month (1-12)
     * @return 200 OK with monthly report details
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable("year") int year,
            @PathVariable("month") int month
    ) {
        MonthlyReportResponse response = reportService.getMonthlyReport(year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves an annual income and expense report.
     *
     * @param year Report year
     * @return 200 OK with yearly report details
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable("year") int year) {
        YearlyReportResponse response = reportService.getYearlyReport(year);
        return ResponseEntity.ok(response);
    }
}
