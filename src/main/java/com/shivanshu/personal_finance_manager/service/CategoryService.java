package com.shivanshu.personal_finance_manager.service;

import com.shivanshu.personal_finance_manager.dto.request.CreateCategoryRequest;
import com.shivanshu.personal_finance_manager.dto.response.CategoryListResponse;
import com.shivanshu.personal_finance_manager.dto.response.CategoryResponse;

/**
 * Service contract for category management operations.
 */
public interface CategoryService {

    /**
     * Retrieves all categories accessible to the current user (defaults and custom).
     *
     * @return CategoryListResponse containing accessible categories
     */
    CategoryListResponse getCategories();

    /**
     * Creates a new custom category for the current user.
     *
     * @param request The category details
     * @return CategoryResponse for the created category
     */
    CategoryResponse createCategory(CreateCategoryRequest request);

    /**
     * Deletes a custom category owned by the current user by name.
     *
     * @param name The category name
     */
    void deleteCategory(String name);
}
