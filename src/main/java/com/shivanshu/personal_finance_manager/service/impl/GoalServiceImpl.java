package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.request.CreateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.response.GoalListResponse;
import com.shivanshu.personal_finance_manager.dto.response.GoalResponse;
import com.shivanshu.personal_finance_manager.entity.SavingsGoal;
import com.shivanshu.personal_finance_manager.entity.UserEntity;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.mapper.GoalMapper;
import com.shivanshu.personal_finance_manager.repository.SavingsGoalRepository;
import com.shivanshu.personal_finance_manager.security.CurrentUserProvider;
import com.shivanshu.personal_finance_manager.service.GoalService;
import com.shivanshu.personal_finance_manager.service.progress.GoalProgressCalculator;
import com.shivanshu.personal_finance_manager.util.MoneyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Concrete implementation of GoalService managing savings goal lifecycle,
 * authorization checks, and live progress calculation.
 */
@Service
@Transactional
public class GoalServiceImpl implements GoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final CurrentUserProvider currentUserProvider;
    private final GoalProgressCalculator goalProgressCalculator;
    private final GoalMapper goalMapper;
    private final Clock clock;

    public GoalServiceImpl(
            SavingsGoalRepository savingsGoalRepository,
            CurrentUserProvider currentUserProvider,
            GoalProgressCalculator goalProgressCalculator,
            GoalMapper goalMapper,
            Clock clock
    ) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.currentUserProvider = currentUserProvider;
        this.goalProgressCalculator = goalProgressCalculator;
        this.goalMapper = goalMapper;
        this.clock = clock;
    }

    @Override
    public GoalResponse createGoal(CreateGoalRequest request) {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = request.startDate() != null ? request.startDate() : today;

        if (!request.targetDate().isAfter(startDate)) {
            throw ApiException.badRequest("targetDate must be after startDate");
        }

        UserEntity userEntity = currentUserProvider.getCurrentUserEntity();
        SavingsGoal goal = new SavingsGoal(
                userEntity,
                request.goalName().trim(),
                MoneyUtils.scale(request.targetAmount()),
                request.targetDate(),
                startDate
        );

        SavingsGoal saved = savingsGoalRepository.save(goal);
        BigDecimal progress = goalProgressCalculator.calculateProgress(saved);
        return goalMapper.toResponse(saved, progress);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalListResponse getGoals() {
        Long userId = currentUserProvider.getCurrentUserId();
        List<SavingsGoal> goals = savingsGoalRepository.findByUserEntityIdOrderByIdAsc(userId);

        List<GoalResponse> responses = goals.stream()
                .map(goal -> {
                    BigDecimal progress = goalProgressCalculator.calculateProgress(goal);
                    return goalMapper.toResponse(goal, progress);
                })
                .toList();

        return new GoalListResponse(responses);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponse getGoalById(Long id) {
        SavingsGoal goal = findGoalAndCheckOwnership(id);
        BigDecimal progress = goalProgressCalculator.calculateProgress(goal);
        return goalMapper.toResponse(goal, progress);
    }

    @Override
    public GoalResponse updateGoal(Long id, UpdateGoalRequest request) {
        SavingsGoal goal = findGoalAndCheckOwnership(id);

        if (request.targetAmount() != null) {
            if (request.targetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw ApiException.badRequest("Target amount must be greater than 0");
            }
            goal.setTargetAmount(MoneyUtils.scale(request.targetAmount()));
        }

        if (request.goalName() != null) {
            if (request.goalName().isBlank()) {
                throw ApiException.badRequest("Goal name cannot be blank");
            }
            goal.setGoalName(request.goalName().trim());
        }

        LocalDate newStartDate = request.startDate() != null ? request.startDate() : goal.getStartDate();
        LocalDate newTargetDate = request.targetDate() != null ? request.targetDate() : goal.getTargetDate();

        if (!newTargetDate.isAfter(newStartDate)) {
            throw ApiException.badRequest("targetDate must be after startDate");
        }

        if (request.startDate() != null) {
            goal.setStartDate(request.startDate());
        }
        if (request.targetDate() != null) {
            goal.setTargetDate(request.targetDate());
        }

        BigDecimal progress = goalProgressCalculator.calculateProgress(goal);
        return goalMapper.toResponse(goal, progress);
    }

    @Override
    public void deleteGoal(Long id) {
        SavingsGoal goal = findGoalAndCheckOwnership(id);
        savingsGoalRepository.delete(goal);
    }

    private SavingsGoal findGoalAndCheckOwnership(Long id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Goal not found"));

        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (!goal.getUser().getId().equals(currentUserId)) {
            throw ApiException.forbidden("Access denied to goal");
        }

        return goal;
    }
}
