package com.shivanshu.personal_finance_manager.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shivanshu.personal_finance_manager.entity.CategoryType;

/**
 * Response payload representing a category.
 *
 * @param name     Category name
 * @param type     Category classification (INCOME or EXPENSE)
 * @param isCustom Indicates whether the category is custom-created by the user
 */
public record CategoryResponse(
        String name,
        CategoryType type,
        @JsonProperty("isCustom")
        boolean isCustom
) {
}
