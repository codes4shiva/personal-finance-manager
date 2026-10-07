package com.shivanshu.personal_finance_manager.controller;

import com.shivanshu.personal_finance_manager.dto.request.CreateCategoryRequest;
import com.shivanshu.personal_finance_manager.dto.response.AuthMessageResponse;
import com.shivanshu.personal_finance_manager.dto.response.CategoryListResponse;
import com.shivanshu.personal_finance_manager.dto.response.CategoryResponse;
import com.shivanshu.personal_finance_manager.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing REST endpoints for category operations.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Retrieves all categories accessible to the authenticated user.
     *
     * @return 200 OK with list of categories
     */
    @GetMapping
    public ResponseEntity<CategoryListResponse> getCategories() {
        CategoryListResponse response = categoryService.getCategories();
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new custom category for the authenticated user.
     *
     * @param request Validated create category payload
     * @return 201 Created with created category
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletes a custom category owned by the authenticated user by name.
     *
     * @param name Name of the custom category to delete
     * @return 200 OK with confirmation message
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<AuthMessageResponse> deleteCategory(@PathVariable("name") String name) {
        categoryService.deleteCategory(name);
        return ResponseEntity.ok(new AuthMessageResponse("Category deleted successfully"));
    }
}
