package com.shivanshu.personal_finance_manager.specification;

import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * JPA Specifications for dynamic querying of Transaction entities.
 */
public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    /**
     * Filters transactions belonging to a specific user ID.
     */
    public static Specification<Transaction> forUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("userEntity").get("id"), userId);
    }

    /**
     * Filters transactions occurring on or after the given start date.
     */
    public static Specification<Transaction> startDateOnOrAfter(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate);
    }

    /**
     * Filters transactions occurring on or before the given end date.
     */
    public static Specification<Transaction> endDateOnOrBefore(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(root.get("transactionDate"), endDate);
    }

    /**
     * Filters transactions associated with a specific category ID.
     */
    public static Specification<Transaction> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Filters transactions matching a category name (case-insensitively).
     */
    public static Specification<Transaction> hasCategoryName(String categoryName) {
        return (root, query, cb) -> (categoryName == null || categoryName.isBlank()) ? null :
                cb.equal(cb.lower(root.get("category").get("name")), categoryName.trim().toLowerCase());
    }

    /**
     * Filters transactions matching a specific category type (INCOME or EXPENSE).
     */
    public static Specification<Transaction> hasType(CategoryType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("category").get("type"), type);
    }
}
