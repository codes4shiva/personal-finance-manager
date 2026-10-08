package com.shivanshu.personal_finance_manager.config;

import com.shivanshu.personal_finance_manager.entity.Category;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
import com.shivanshu.personal_finance_manager.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes default system categories upon application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoryRepository categoryRepository;

    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDefaultCategory("Salary", CategoryType.INCOME);
        seedDefaultCategory("Freelance", CategoryType.INCOME);
        seedDefaultCategory("Food", CategoryType.EXPENSE);
        seedDefaultCategory("Rent", CategoryType.EXPENSE);
        seedDefaultCategory("Transportation", CategoryType.EXPENSE);
        seedDefaultCategory("Entertainment", CategoryType.EXPENSE);
        seedDefaultCategory("Healthcare", CategoryType.EXPENSE);
        seedDefaultCategory("Utilities", CategoryType.EXPENSE);
    }

    private void seedDefaultCategory(String name, CategoryType type) {
        if (!categoryRepository.existsDefaultByNameIgnoreCase(name)) {
            Category category = new Category(name, type, false, null);
            categoryRepository.save(category);
            log.info("Initialized default category: {} ({})", name, type);
        }
    }
}
