package com.shivanshu.personal_finance_manager.repository;

import com.shivanshu.personal_finance_manager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Retrieves all categories accessible to a user (defaults plus user's custom categories).
     *
     * @param userId The ID of the logged-in user
     * @return List of accessible categories ordered by ID
     */
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user.id = :userId ORDER BY c.id ASC")
    List<Category> findAllAccessibleByUserId(@Param("userId") Long userId);

    /**
     * Finds an accessible category by its name, case-insensitively.
     *
     * @param userId The ID of the logged-in user
     * @param name   Category name
     * @return Optional Category
     */
    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user.id = :userId) AND LOWER(c.name) = LOWER(:name)")
    Optional<Category> findAccessibleByName(@Param("userId") Long userId, @Param("name") String name);

    /**
     * Finds a system default category by name, case-insensitively.
     *
     * @param name Category name
     * @return Optional Category
     */
    @Query("SELECT c FROM Category c WHERE c.user IS NULL AND LOWER(c.name) = LOWER(:name)")
    Optional<Category> findDefaultByNameIgnoreCase(@Param("name") String name);

    /**
     * Checks if a system default category exists with the given name, case-insensitively.
     *
     * @param name Category name
     * @return true if a default category exists
     */
    @Query("SELECT (COUNT(c) > 0) FROM Category c WHERE c.user IS NULL AND LOWER(c.name) = LOWER(:name)")
    boolean existsDefaultByNameIgnoreCase(@Param("name") String name);

    /**
     * Checks if an accessible category (default or custom) already exists for a user with the given name.
     *
     * @param userId The ID of the user
     * @param name   Category name
     * @return true if a category with this name exists for this user or as a default
     */
    @Query("SELECT (COUNT(c) > 0) FROM Category c WHERE (c.user IS NULL OR c.user.id = :userId) AND LOWER(c.name) = LOWER(:name)")
    boolean existsAccessibleByNameIgnoreCase(@Param("userId") Long userId, @Param("name") String name);

    /**
     * Finds a custom category owned by a specific user by name, case-insensitively.
     *
     * @param userId The ID of the user
     * @param name   Category name
     * @return Optional Category
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND LOWER(c.name) = LOWER(:name)")
    Optional<Category> findCustomByUserAndNameIgnoreCase(@Param("userId") Long userId, @Param("name") String name);
}
