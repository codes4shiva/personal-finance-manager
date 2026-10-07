package com.shivanshu.personal_finance_manager.dto.response;

import java.util.List;

/**
 * Response payload wrapping a list of transactions.
 *
 * @param transactions List of transactions
 */
public record TransactionListResponse(
        List<TransactionResponse> transactions
) {
}
