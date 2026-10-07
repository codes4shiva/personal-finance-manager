package com.shivanshu.personal_finance_manager.service.report;

/**
 * Extension point contract for generating period-specific financial reports.
 *
 * @param <T> The report response type
 * @param <C> The period criteria type
 */
public interface ReportGenerator<T, C> {

    /**
     * Generates a report for the specified user and criteria.
     *
     * @param userId   The user ID
     * @param criteria Period parameters
     * @return The generated report response
     */
    T generate(Long userId, C criteria);
}
