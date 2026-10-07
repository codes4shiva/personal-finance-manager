package com.shivanshu.personal_finance_manager.dto.response;

import java.util.List;

/**
 * Response payload wrapping a list of savings goals.
 *
 * @param goals List of savings goals
 */
public record GoalListResponse(
        List<GoalResponse> goals
) {
}
