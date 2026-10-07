package com.shivanshu.personal_finance_manager.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility methods for handling monetary amounts with scale 2 and RoundingMode.HALF_UP.
 */
public final class MoneyUtils {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);

    private MoneyUtils() {
    }

    /**
     * Standardizes a BigDecimal amount to 2 decimal places with HALF_UP rounding.
     * Returns 0.00 if the provided amount is null.
     *
     * @param amount The raw BigDecimal amount
     * @return Scaled BigDecimal with scale 2
     */
    public static BigDecimal scale(BigDecimal amount) {
        if (amount == null) {
            return ZERO;
        }
        return amount.setScale(SCALE, ROUNDING_MODE);
    }
}
