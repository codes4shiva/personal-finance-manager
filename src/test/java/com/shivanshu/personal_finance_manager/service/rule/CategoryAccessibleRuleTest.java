package com.shivanshu.personal_finance_manager.service.rule;

import com.shivanshu.personal_finance_manager.entity.Category;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.entity.User;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for CategoryAccessibleRule.
 */
class CategoryAccessibleRuleTest {

    private CategoryAccessibleRule rule;

    @BeforeEach
    void setUp() {
        rule = new CategoryAccessibleRule();
    }

    @Test
    void validate_whenCategoryIsNull_throwsBadRequest() {
        Transaction tx = new Transaction();
        tx.setCategory(null);

        ApiException ex = assertThrows(ApiException.class, () -> rule.validate(tx));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void validate_whenCategoryIsDefault_doesNotThrow() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setUser(null); // default category

        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setCategory(category);

        assertDoesNotThrow(() -> rule.validate(tx));
    }

    @Test
    void validate_whenCategoryBelongsToUser_doesNotThrow() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setUser(user);

        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setCategory(category);

        assertDoesNotThrow(() -> rule.validate(tx));
    }

    @Test
    void validate_whenCategoryBelongsToAnotherUser_throwsBadRequest() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        Category category = new Category();
        category.setUser(user2);

        Transaction tx = new Transaction();
        tx.setUser(user1);
        tx.setCategory(category);

        ApiException ex = assertThrows(ApiException.class, () -> rule.validate(tx));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Category is not accessible", ex.getMessage());
    }
}
