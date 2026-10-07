package com.shivanshu.personal_finance_manager.service;

import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.response.TransactionListResponse;
import com.shivanshu.personal_finance_manager.dto.response.TransactionResponse;
import com.shivanshu.personal_finance_manager.entity.CategoryType;

import java.time.LocalDate;

/**
 * Service contract for financial transaction operations.
 */
public interface TransactionService {

    /**
     * Creates a new transaction after validating business rules.
     *
     * @param request Transaction creation details
     * @return Created TransactionResponse
     */
    TransactionResponse createTransaction(CreateTransactionRequest request);

    /**
     * Retrieves transactions for the current user matching optional filter criteria,
     * ordered newest first (date descending, then id descending).
     *
     * @param startDate    Optional start date filter (inclusive)
     * @param endDate      Optional end date filter (inclusive)
     * @param categoryId   Optional category ID filter
     * @param categoryName Optional category name filter
     * @param type         Optional category type filter (INCOME or EXPENSE)
     * @return TransactionListResponse containing matching transactions
     */
    TransactionListResponse getTransactions(
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            String categoryName,
            CategoryType type
    );

    /**
     * Updates an existing transaction. Date is immutable.
     *
     * @param id      Transaction ID
     * @param request Update fields
     * @return Updated TransactionResponse
     */
    TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request);

    /**
     * Hard-deletes a transaction belonging to the current user.
     *
     * @param id Transaction ID
     */
    void deleteTransaction(Long id);
}
