package com.shivanshu.personal_finance_manager.service.rule;

import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Validates that transaction dates are not set in the future according to the configured clock.
 */
@Component
public class NotFutureDateRule implements TransactionRule {

    private final Clock clock;

    public NotFutureDateRule(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void validate(Transaction transaction) {
        LocalDate today = LocalDate.now(clock);
        if (transaction.getTransactionDate() != null && transaction.getTransactionDate().isAfter(today)) {
            throw ApiException.badRequest("Transaction date cannot be in the future");
        }
    }
}
