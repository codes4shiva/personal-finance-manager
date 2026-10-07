package com.shivanshu.personal_finance_manager.mapper;

import com.shivanshu.personal_finance_manager.dto.response.CategoryResponse;
import com.shivanshu.personal_finance_manager.entity.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper converting between Category entities and DTO representations.
 */
@Component
public class CategoryMapper {

    /**
     * Converts a Category entity to a CategoryResponse DTO.
     *
     * @param category The entity to convert
     * @return CategoryResponse DTO
     */
    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(
                category.getName(),
                category.getType(),
                category.isCustom()
        );
    }
}
