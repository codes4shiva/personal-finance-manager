package com.shivanshu.personal_finance_manager.controller;

import com.shivanshu.personal_finance_manager.dto.request.CreateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.response.AuthMessageResponse;
import com.shivanshu.personal_finance_manager.dto.response.GoalListResponse;
import com.shivanshu.personal_finance_manager.dto.response.GoalResponse;
import com.shivanshu.personal_finance_manager.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing REST endpoints for savings goal operations.
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    /**
     * Creates a new savings goal.
     *
     * @param request Validated goal creation payload
     * @return 201 Created with created goal and progress metrics
     */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody CreateGoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all savings goals for the authenticated user.
     *
     * @return 200 OK with list of goals
     */
    @GetMapping
    public ResponseEntity<GoalListResponse> getGoals() {
        GoalListResponse response = goalService.getGoals();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single savings goal by ID.
     *
     * @param id Goal ID
     * @return 200 OK with goal details
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable("id") Long id) {
        GoalResponse response = goalService.getGoalById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing savings goal.
     *
     * @param id      Goal ID
     * @param request Validated update payload
     * @return 200 OK with updated goal details
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateGoalRequest request
    ) {
        GoalResponse response = goalService.updateGoal(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a savings goal by ID.
     *
     * @param id Goal ID
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<AuthMessageResponse> deleteGoal(@PathVariable("id") Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(new AuthMessageResponse("Goal deleted successfully"));
    }
}
