package com.shivanshu.personal_finance_manager.service.rule;

import com.shivanshu.personal_finance_manager.entity.Category;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import org.springframework.stereotype.Component;

/**
 * Validates that the category referenced by a transaction is accessible to the owning user.
 */
@Component
public class CategoryAccessibleRule implements TransactionRule {

    @Override
    public void validate(Transaction transaction) {
        Category category = transaction.getCategory();
        if (category == null) {
            throw ApiException.badRequest("Category is required");
        }

        if (category.getUser() != null && transaction.getUser() != null
                && !category.getUser().getId().equals(transaction.getUser().getId())) {
            throw ApiException.badRequest("Category is not accessible");
        }
    }
}
