package com.shivanshu.personal_finance_manager.controller;

import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.response.AuthMessageResponse;
import com.shivanshu.personal_finance_manager.dto.response.TransactionListResponse;
import com.shivanshu.personal_finance_manager.dto.response.TransactionResponse;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller exposing REST endpoints for transaction CRUD and search operations.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Creates a new financial transaction.
     *
     * @param request Validated transaction request payload
     * @return 201 Created with created transaction
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves transactions for the authenticated user, optionally filtered.
     *
     * @param startDate    Optional start date filter (YYYY-MM-DD)
     * @param endDate      Optional end date filter (YYYY-MM-DD)
     * @param categoryId   Optional category ID filter
     * @param categoryName Optional category name filter (supports ?category= or ?categoryName=)
     * @param type         Optional transaction type filter (INCOME or EXPENSE)
     * @return 200 OK with list of transactions
     */
    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "category", required = false) String categoryName,
            @RequestParam(name = "type", required = false) CategoryType type
    ) {
        TransactionListResponse response = transactionService.getTransactions(
                startDate,
                endDate,
                categoryId,
                categoryName,
                type
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing transaction.
     *
     * @param id      Transaction ID
     * @param request Validated update fields
     * @return 200 OK with updated transaction
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTransactionRequest request
    ) {
        TransactionResponse response = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Hard deletes a transaction.
     *
     * @param id Transaction ID
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<AuthMessageResponse> deleteTransaction(@PathVariable("id") Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(new AuthMessageResponse("Transaction deleted successfully"));
    }
}
