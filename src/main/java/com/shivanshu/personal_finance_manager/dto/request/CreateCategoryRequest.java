package com.shivanshu.personal_finance_manager.dto.request;

import com.shivanshu.personal_finance_manager.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for creating a custom category.
 *
 * @param name Category name
 * @param type Category classification (INCOME or EXPENSE)
 */
public record CreateCategoryRequest(
        @NotBlank(message = "Category name is required")
        String name,

        @NotNull(message = "Category type is required (INCOME or EXPENSE)")
        CategoryType type
) {
}
