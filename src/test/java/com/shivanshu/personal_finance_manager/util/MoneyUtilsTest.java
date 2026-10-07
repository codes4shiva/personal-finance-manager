package com.shivanshu.personal_finance_manager.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for MoneyUtils.
 */
class MoneyUtilsTest {

    @Test
    void scale_whenNull_returnsZeroWithScaleTwo() {
        BigDecimal result = MoneyUtils.scale(null);
        assertEquals(new BigDecimal("0.00"), result);
        assertEquals(2, result.scale());
    }

    @Test
    void scale_roundsHalfUp() {
        BigDecimal val1 = new BigDecimal("10.554");
        assertEquals(new BigDecimal("10.55"), MoneyUtils.scale(val1));

        BigDecimal val2 = new BigDecimal("10.555");
        assertEquals(new BigDecimal("10.56"), MoneyUtils.scale(val2));

        BigDecimal val3 = new BigDecimal("10");
        assertEquals(new BigDecimal("10.00"), MoneyUtils.scale(val3));
    }
}
