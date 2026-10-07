package com.shivanshu.personal_finance_manager.service.rule;

import com.shivanshu.personal_finance_manager.entity.Transaction;

/**
 * Extension point interface for validating transactions before persistence.
 */
public interface TransactionRule {

    /**
     * Validates a transaction against business constraints.
     *
     * @param transaction The transaction entity to validate
     * @throws com.shivanshu.personal_finance_manager.exception.ApiException if validation fails
     */
    void validate(Transaction transaction);
}
