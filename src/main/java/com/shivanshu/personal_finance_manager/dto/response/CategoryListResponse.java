package com.shivanshu.personal_finance_manager.dto.response;

import java.util.List;

/**
 * Response payload wrapping a list of categories.
 *
 * @param categories List of accessible categories
 */
public record CategoryListResponse(
        List<CategoryResponse> categories
) {
}
