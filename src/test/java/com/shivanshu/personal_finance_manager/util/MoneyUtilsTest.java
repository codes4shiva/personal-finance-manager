package com.shivanshu.personal_finance_manager.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyUtilsTest {

    @Test
    @DisplayName("Scale null returns 0.00")
    void testScaleNull() {
        BigDecimal result = MoneyUtils.scale(null);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    @DisplayName("Scale non-null standardizes to 2 decimal places HALF_UP")
    void testScaleNonNull() {
        BigDecimal result = MoneyUtils.scale(new BigDecimal("123.456"));
        assertEquals(new BigDecimal("123.46"), result);

        BigDecimal roundedDown = MoneyUtils.scale(new BigDecimal("123.454"));
        assertEquals(new BigDecimal("123.45"), result);
    }
}
