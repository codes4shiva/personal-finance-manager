package com.shivanshu.personal_finance_manager.service;

import com.shivanshu.personal_finance_manager.dto.request.CreateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.response.GoalListResponse;
import com.shivanshu.personal_finance_manager.dto.response.GoalResponse;

/**
 * Service contract for savings goal management operations.
 */
public interface GoalService {

    /**
     * Creates a new savings goal for the current user.
     *
     * @param request Goal creation parameters
     * @return Created GoalResponse
     */
    GoalResponse createGoal(CreateGoalRequest request);

    /**
     * Retrieves all savings goals belonging to the current user.
     *
     * @return GoalListResponse containing all goals with calculated progress
     */
    GoalListResponse getGoals();

    /**
     * Retrieves a single savings goal by ID for the current user.
     *
     * @param id Goal ID
     * @return GoalResponse
     */
    GoalResponse getGoalById(Long id);

    /**
     * Updates an existing savings goal.
     *
     * @param id      Goal ID
     * @param request Update parameters
     * @return Updated GoalResponse
     */
    GoalResponse updateGoal(Long id, UpdateGoalRequest request);

    /**
     * Hard-deletes a savings goal by ID.
     *
     * @param id Goal ID
     */
    void deleteGoal(Long id);
}
