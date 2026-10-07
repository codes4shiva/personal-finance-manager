package com.shivanshu.personal_finance_manager.service.rule;

import com.shivanshu.personal_finance_manager.entity.Transaction;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for NotFutureDateRule.
 */
class NotFutureDateRuleTest {

    private NotFutureDateRule rule;
    private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");

    @BeforeEach
    void setUp() {
        // Fixed date: 2026-10-07
        Instant fixedInstant = Instant.parse("2026-10-07T12:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);
        rule = new NotFutureDateRule(fixedClock);
    }

    @Test
    void validate_whenDateIsToday_doesNotThrow() {
        Transaction tx = new Transaction();
        tx.setTransactionDate(LocalDate.of(2026, 10, 7));

        assertDoesNotThrow(() -> rule.validate(tx));
    }

    @Test
    void validate_whenDateIsInPast_doesNotThrow() {
        Transaction tx = new Transaction();
        tx.setTransactionDate(LocalDate.of(2026, 10, 6));

        assertDoesNotThrow(() -> rule.validate(tx));
    }

    @Test
    void validate_whenDateIsInFuture_throwsBadRequest() {
        Transaction tx = new Transaction();
        tx.setTransactionDate(LocalDate.of(2026, 10, 8));

        ApiException ex = assertThrows(ApiException.class, () -> rule.validate(tx));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Transaction date cannot be in the future", ex.getMessage());
    }
}
