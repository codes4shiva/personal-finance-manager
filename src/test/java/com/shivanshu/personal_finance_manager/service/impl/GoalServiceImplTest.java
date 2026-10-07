package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.request.CreateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.response.GoalListResponse;
import com.shivanshu.personal_finance_manager.dto.response.GoalResponse;
import com.shivanshu.personal_finance_manager.entity.SavingsGoal;
import com.shivanshu.personal_finance_manager.entity.User;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.mapper.GoalMapper;
import com.shivanshu.personal_finance_manager.repository.SavingsGoalRepository;
import com.shivanshu.personal_finance_manager.security.CurrentUserProvider;
import com.shivanshu.personal_finance_manager.service.progress.GoalProgressCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GoalServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private GoalProgressCalculator goalProgressCalculator;

    private final GoalMapper goalMapper = new GoalMapper();
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-07T10:00:00Z"), ZoneId.of("Asia/Kolkata"));

    private GoalServiceImpl goalService;

    @BeforeEach
    void setUp() {
        goalService = new GoalServiceImpl(
                savingsGoalRepository,
                currentUserProvider,
                goalProgressCalculator,
                goalMapper,
                clock
        );
    }

    @Test
    void createGoal_success() {
        User user = new User();
        user.setId(1L);
        when(currentUserProvider.getCurrentUserEntity()).thenReturn(user);

        SavingsGoal saved = new SavingsGoal(
                user,
                "New Car",
                new BigDecimal("50000.00"),
                LocalDate.of(2027, 10, 7),
                LocalDate.of(2026, 10, 7)
        );
        saved.setId(1L);
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(saved);
        when(goalProgressCalculator.calculateProgress(saved)).thenReturn(new BigDecimal("10000.00"));

        CreateGoalRequest request = new CreateGoalRequest(
                "New Car",
                new BigDecimal("50000.00"),
                LocalDate.of(2027, 10, 7),
                null
        );

        GoalResponse response = goalService.createGoal(request);

        assertNotNull(response);
        assertEquals("New Car", response.goalName());
        assertEquals(new BigDecimal("10000.00"), response.currentProgress());
        assertEquals(new BigDecimal("20.00"), response.progressPercentage());
        assertEquals(new BigDecimal("40000.00"), response.remainingAmount());
    }

    @Test
    void createGoal_targetDateInPast_throwsBadRequest() {
        CreateGoalRequest request = new CreateGoalRequest(
                "Vacation",
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 10, 6), // yesterday
                null
        );

        ApiException ex = assertThrows(ApiException.class, () -> goalService.createGoal(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("targetDate must be in the future", ex.getMessage());
    }

    @Test
    void createGoal_startDateAfterTargetDate_throwsBadRequest() {
        CreateGoalRequest request = new CreateGoalRequest(
                "Vacation",
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 15) // startDate after targetDate
        );

        ApiException ex = assertThrows(ApiException.class, () -> goalService.createGoal(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("startDate cannot be after targetDate", ex.getMessage());
    }

    @Test
    void getGoalById_otherUser_throwsForbidden() {
        User owner = new User();
        owner.setId(2L);

        SavingsGoal goal = new SavingsGoal();
        goal.setId(10L);
        goal.setUser(owner);

        when(savingsGoalRepository.findById(10L)).thenReturn(Optional.of(goal));
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L); // logged in as user 1

        ApiException ex = assertThrows(ApiException.class, () -> goalService.getGoalById(10L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("Access denied to goal", ex.getMessage());
    }

    @Test
    void getGoalById_notFound_throwsNotFound() {
        when(savingsGoalRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> goalService.getGoalById(999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void updateGoal_neitherFieldProvided_throwsBadRequest() {
        User user = new User();
        user.setId(1L);
        SavingsGoal goal = new SavingsGoal();
        goal.setId(10L);
        goal.setUser(user);

        when(savingsGoalRepository.findById(10L)).thenReturn(Optional.of(goal));
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        UpdateGoalRequest request = new UpdateGoalRequest(null, null);

        ApiException ex = assertThrows(ApiException.class, () -> goalService.updateGoal(10L, request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void deleteGoal_success() {
        User user = new User();
        user.setId(1L);
        SavingsGoal goal = new SavingsGoal();
        goal.setId(10L);
        goal.setUser(user);

        when(savingsGoalRepository.findById(10L)).thenReturn(Optional.of(goal));
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        assertDoesNotThrow(() -> goalService.deleteGoal(10L));
        verify(savingsGoalRepository).delete(goal);
    }
}
