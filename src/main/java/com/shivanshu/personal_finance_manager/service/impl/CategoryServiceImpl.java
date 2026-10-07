package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.request.CreateCategoryRequest;
import com.shivanshu.personal_finance_manager.dto.response.CategoryListResponse;
import com.shivanshu.personal_finance_manager.dto.response.CategoryResponse;
import com.shivanshu.personal_finance_manager.entity.Category;
import com.shivanshu.personal_finance_manager.entity.User;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.mapper.CategoryMapper;
import com.shivanshu.personal_finance_manager.repository.CategoryRepository;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import com.shivanshu.personal_finance_manager.security.CurrentUserProvider;
import com.shivanshu.personal_finance_manager.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of CategoryService managing category creation, listing, and deletion.
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            CurrentUserProvider currentUserProvider,
            CategoryMapper categoryMapper
    ) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserProvider = currentUserProvider;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryListResponse getCategories() {
        Long userId = currentUserProvider.getCurrentUserId();
        List<CategoryResponse> categories = categoryRepository.findAllAccessibleByUserId(userId)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();

        return new CategoryListResponse(categories);
    }

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        String trimmedName = request.name().trim();

        if (categoryRepository.existsAccessibleByNameIgnoreCase(userId, trimmedName)) {
            throw ApiException.conflict("Category with name '" + trimmedName + "' already exists");
        }

        User user = currentUserProvider.getCurrentUserEntity();
        Category category = new Category(trimmedName, request.type(), true, user);
        Category saved = categoryRepository.save(category);

        return categoryMapper.toResponse(saved);
    }

    @Override
    public void deleteCategory(String name) {
        if (name == null || name.isBlank()) {
            throw ApiException.badRequest("Category name is required");
        }

        String trimmedName = name.trim();
        Long userId = currentUserProvider.getCurrentUserId();

        if (categoryRepository.existsDefaultByNameIgnoreCase(trimmedName)) {
            throw ApiException.forbidden("Default category cannot be deleted");
        }

        Category category = categoryRepository.findCustomByUserAndNameIgnoreCase(userId, trimmedName)
                .orElseThrow(() -> ApiException.notFound("Category not found: " + trimmedName));

        if (transactionRepository.existsByCategoryId(category.getId())) {
            throw ApiException.badRequest("Cannot delete category as it is currently associated with transactions");
        }

        categoryRepository.delete(category);
    }
}
