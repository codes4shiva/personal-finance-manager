package com.shivanshu.personal_finance_manager.mapper;

import com.shivanshu.personal_finance_manager.dto.response.CategoryResponse;
import com.shivanshu.personal_finance_manager.dto.response.GoalResponse;
import com.shivanshu.personal_finance_manager.dto.response.TransactionResponse;
import com.shivanshu.personal_finance_manager.entity.Category;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.SavingsGoal;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MappersTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final GoalMapper goalMapper = new GoalMapper();
    private final TransactionMapper transactionMapper = new TransactionMapper();

    @Test
    @DisplayName("CategoryMapper maps valid entity and null")
    void testCategoryMapper() {
        assertNull(categoryMapper.toResponse(null));

        Category category = new Category("Salary", CategoryType.INCOME, false, null);
        category.setId(10L);
        CategoryResponse response = categoryMapper.toResponse(category);

        assertNotNull(response);
        assertEquals("Salary", response.name());
        assertEquals(CategoryType.INCOME, response.type());
        assertFalse(response.isCustom());
    }

    @Test
    @DisplayName("GoalMapper maps valid entity, progress, zero target, and null")
    void testGoalMapper() {
        assertNull(goalMapper.toResponse(null, BigDecimal.TEN));

        SavingsGoal goal = new SavingsGoal(
                new UserEntity("test@example.com", "pass", "Test", "+1234567890"),
                "Trip",
                new BigDecimal("5000.00"),
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2025, 1, 1)
        );
        goal.setId(1L);

        GoalResponse response = goalMapper.toResponse(goal, new BigDecimal("1000.00"));
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(new BigDecimal("1000.00"), response.currentProgress());
        assertEquals(new BigDecimal("20.00"), response.progressPercentage());
        assertEquals(new BigDecimal("4000.00"), response.remainingAmount());

        // Target amount 0 or negative branch
        goal.setTargetAmount(BigDecimal.ZERO);
        GoalResponse zeroTargetResponse = goalMapper.toResponse(goal, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("0.00"), zeroTargetResponse.progressPercentage());
        assertEquals(new BigDecimal("0.00"), zeroTargetResponse.remainingAmount());

        // Negative progress branch
        GoalResponse negativeProgressResponse = goalMapper.toResponse(goal, new BigDecimal("-50.00"));
        assertEquals(new BigDecimal("0.00"), negativeProgressResponse.progressPercentage());
    }

    @Test
    @DisplayName("TransactionMapper maps valid entity, null, and category null")
    void testTransactionMapper() {
        assertNull(transactionMapper.toResponse(null));

        Transaction tx = new Transaction(
                new UserEntity("test@example.com", "pass", "Test", "+1234567890"),
                null,
                new BigDecimal("250.00"),
                LocalDate.of(2025, 1, 15),
                "Groceries"
        );
        tx.setId(5L);

        TransactionResponse response = transactionMapper.toResponse(tx);
        assertNotNull(response);
        assertEquals(5L, response.id());
        assertNull(response.category());
        assertNull(response.type());

        Category cat = new Category("Food", CategoryType.EXPENSE, false, null);
        tx.setCategory(cat);
        TransactionResponse responseWithCat = transactionMapper.toResponse(tx);
        assertEquals("Food", responseWithCat.category());
        assertEquals(CategoryType.EXPENSE, responseWithCat.type());
    }
}
