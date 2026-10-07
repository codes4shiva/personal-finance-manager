package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.request.CreateCategoryRequest;
import com.shivanshu.personal_finance_manager.dto.response.CategoryListResponse;
import com.shivanshu.personal_finance_manager.dto.response.CategoryResponse;
import com.shivanshu.personal_finance_manager.entity.Category;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.entity.User;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.mapper.CategoryMapper;
import com.shivanshu.personal_finance_manager.repository.CategoryRepository;
import com.shivanshu.personal_finance_manager.repository.TransactionRepository;
import com.shivanshu.personal_finance_manager.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private com.shivanshu.personal_finance_manager.repository.TransactionRepository transactionRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private final CategoryMapper categoryMapper = new CategoryMapper();

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository, transactionRepository, currentUserProvider, categoryMapper);
    }

    @Test
    void getCategories_returnsAccessibleCategories() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        Category defaultCat = new Category("Salary", CategoryType.INCOME, false, null);
        Category customCat = new Category("Investments", CategoryType.INCOME, true, new User());
        when(categoryRepository.findAllAccessibleByUserId(1L)).thenReturn(List.of(defaultCat, customCat));

        CategoryListResponse response = categoryService.getCategories();
        assertEquals(2, response.categories().size());
        assertEquals("Salary", response.categories().get(0).name());
        assertFalse(response.categories().get(0).isCustom());
        assertEquals("Investments", response.categories().get(1).name());
        assertTrue(response.categories().get(1).isCustom());
    }

    @Test
    void createCategory_success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.existsAccessibleByNameIgnoreCase(1L, "Bonus")).thenReturn(false);

        User user = new User();
        user.setId(1L);
        when(currentUserProvider.getCurrentUserEntity()).thenReturn(user);

        Category saved = new Category("Bonus", CategoryType.INCOME, true, user);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.createCategory(new CreateCategoryRequest("Bonus", CategoryType.INCOME));
        assertEquals("Bonus", response.name());
        assertEquals(CategoryType.INCOME, response.type());
        assertTrue(response.isCustom());
    }

    @Test
    void createCategory_duplicateName_throwsConflict() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.existsAccessibleByNameIgnoreCase(1L, "Food")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> categoryService.createCategory(new CreateCategoryRequest("Food", CategoryType.EXPENSE)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void deleteCategory_defaultCategory_throwsForbidden() {
        when(categoryRepository.existsDefaultByNameIgnoreCase("Salary")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> categoryService.deleteCategory("Salary"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void deleteCategory_customCategoryNotFound_throwsNotFound() {
        when(categoryRepository.existsDefaultByNameIgnoreCase("Unknown")).thenReturn(false);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findCustomByUserAndNameIgnoreCase(1L, "Unknown")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> categoryService.deleteCategory("Unknown"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void deleteCategory_withExistingTransactions_throwsBadRequest() {
        when(categoryRepository.existsDefaultByNameIgnoreCase("CustomGas")).thenReturn(false);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        Category cat = new Category("CustomGas", CategoryType.EXPENSE, true, new User());
        cat.setId(50L);
        when(categoryRepository.findCustomByUserAndNameIgnoreCase(1L, "CustomGas")).thenReturn(Optional.of(cat));
        when(transactionRepository.existsByCategoryId(50L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> categoryService.deleteCategory("CustomGas"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_success() {
        when(categoryRepository.existsDefaultByNameIgnoreCase("CustomGas")).thenReturn(false);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        Category cat = new Category("CustomGas", CategoryType.EXPENSE, true, new User());
        cat.setId(50L);
        when(categoryRepository.findCustomByUserAndNameIgnoreCase(1L, "CustomGas")).thenReturn(Optional.of(cat));
        when(transactionRepository.existsByCategoryId(50L)).thenReturn(false);

        assertDoesNotThrow(() -> categoryService.deleteCategory("CustomGas"));
        verify(categoryRepository).delete(cat);
    }
}
