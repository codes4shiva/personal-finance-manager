package com.shivanshu.personal_finance_manager.repository;

import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Transaction entity operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /**
     * Finds a transaction scoped to a specific user.
     *
     * @param id     Transaction ID
     * @param userId User ID
     * @return Optional Transaction
     */
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /**
     * Checks if any transaction exists for a given category.
     *
     * @param categoryId Category ID
     * @return true if at least one transaction references the category
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * Sums the transaction amounts for a user by category type on or after a given start date.
     *
     * @param userId    User ID
     * @param type      CategoryType (INCOME or EXPENSE)
     * @param startDate Minimum transaction date (inclusive)
     * @return Total sum of transactions or null if none
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.category.type = :type AND t.transactionDate >= :startDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateOnOrAfter(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("startDate") LocalDate startDate
    );

    /**
     * Finds transactions for a user within a specified date range.
     *
     * @param userId    User ID
     * @param startDate Start date inclusive
     * @param endDate   End date inclusive
     * @return List of matching transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    List<Transaction> findByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
