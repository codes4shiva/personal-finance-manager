package com.shivanshu.personal_finance_manager.service.rule;

import com.shivanshu.personal_finance_manager.entity.Category;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.entity.UserEntity;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionRulesTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneId.of("UTC"));
    private final NotFutureDateRule notFutureDateRule = new NotFutureDateRule(fixedClock);
    private final CategoryAccessibleRule categoryAccessibleRule = new CategoryAccessibleRule();

    @Test
    @DisplayName("NotFutureDateRule allows past or current date")
    void testNotFutureDateValid() {
        Transaction tx = new Transaction();
        tx.setTransactionDate(LocalDate.of(2026, 1, 10));
        assertDoesNotThrow(() -> notFutureDateRule.validate(tx));

        tx.setTransactionDate(LocalDate.of(2026, 1, 15));
        assertDoesNotThrow(() -> notFutureDateRule.validate(tx));

        tx.setTransactionDate(null);
        assertDoesNotThrow(() -> notFutureDateRule.validate(tx));
    }

    @Test
    @DisplayName("NotFutureDateRule rejects future date")
    void testNotFutureDateInvalid() {
        Transaction tx = new Transaction();
        tx.setTransactionDate(LocalDate.of(2026, 1, 16));
        assertThrows(ApiException.class, () -> notFutureDateRule.validate(tx));
    }

    @Test
    @DisplayName("CategoryAccessibleRule validates accessibility")
    void testCategoryAccessibleRule() {
        Transaction tx = new Transaction();

        // null category -> bad request
        tx.setCategory(null);
        assertThrows(ApiException.class, () -> categoryAccessibleRule.validate(tx));

        // default category (user null) -> allowed
        Category defaultCat = new Category("Salary", CategoryType.INCOME, false, null);
        tx.setCategory(defaultCat);
        assertDoesNotThrow(() -> categoryAccessibleRule.validate(tx));

        // category owned by same user -> allowed
        UserEntity user1 = new UserEntity("u1@test.com", "pass", "User1", "123");
        user1.setId(1L);
        Category user1Cat = new Category("Custom1", CategoryType.INCOME, true, user1);
        tx.setUser(user1);
        tx.setCategory(user1Cat);
        assertDoesNotThrow(() -> categoryAccessibleRule.validate(tx));

        // category owned by another user -> forbidden/bad request
        UserEntity user2 = new UserEntity("u2@test.com", "pass", "User2", "456");
        user2.setId(2L);
        Category user2Cat = new Category("Custom2", CategoryType.INCOME, true, user2);
        tx.setCategory(user2Cat);
        assertThrows(ApiException.class, () -> categoryAccessibleRule.validate(tx));
    }
}
