package com.shivanshu.personal_finance_manager.repository;

import com.shivanshu.personal_finance_manager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
        SELECT c FROM Category c
        WHERE c.userEntity IS NULL
           OR c.userEntity.id = :userId
        ORDER BY c.id ASC
    """)
    List<Category> findAllAccessibleByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT c FROM Category c
        WHERE (c.userEntity IS NULL OR c.userEntity.id = :userId)
          AND LOWER(c.name) = LOWER(:name)
    """)
    Optional<Category> findAccessibleByName(
            @Param("userId") Long userId,
            @Param("name") String name
    );

    @Query("""
        SELECT c FROM Category c
        WHERE c.userEntity IS NULL
          AND LOWER(c.name) = LOWER(:name)
    """)
    Optional<Category> findDefaultByNameIgnoreCase(
            @Param("name") String name
    );

    @Query("""
        SELECT (COUNT(c) > 0) FROM Category c
        WHERE c.userEntity IS NULL
          AND LOWER(c.name) = LOWER(:name)
    """)
    boolean existsDefaultByNameIgnoreCase(
            @Param("name") String name
    );

    @Query("""
        SELECT (COUNT(c) > 0) FROM Category c
        WHERE (c.userEntity IS NULL OR c.userEntity.id = :userId)
          AND LOWER(c.name) = LOWER(:name)
    """)
    boolean existsAccessibleByNameIgnoreCase(
            @Param("userId") Long userId,
            @Param("name") String name
    );

    @Query("""
        SELECT c FROM Category c
        WHERE c.userEntity.id = :userId
          AND LOWER(c.name) = LOWER(:name)
    """)
    Optional<Category> findCustomByUserAndNameIgnoreCase(
            @Param("userId") Long userId,
            @Param("name") String name
    );
}