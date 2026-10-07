package com.shivanshu.personal_finance_manager.repository;

import com.shivanshu.personal_finance_manager.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for SavingsGoal entity operations.
 */
@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    /**
     * Retrieves all savings goals for a user ordered by ID.
     *
     * @param userId User ID
     * @return List of savings goals
     */
    List<SavingsGoal> findByUserIdOrderByIdAsc(Long userId);

    /**
     * Finds a savings goal by its ID and owning user ID.
     *
     * @param id     Goal ID
     * @param userId User ID
     * @return Optional SavingsGoal
     */
    Optional<SavingsGoal> findByIdAndUserId(Long id, Long userId);
}
