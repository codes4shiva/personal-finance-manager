package com.shivanshu.personal_finance_manager.repository;

import com.shivanshu.personal_finance_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username (email) ignoring case.
     *
     * @param username Email address
     * @return Optional containing the User if found
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * Checks whether a user exists with the given username (email) ignoring case.
     *
     * @param username Email address
     * @return true if a user exists with this username
     */
    boolean existsByUsernameIgnoreCase(String username);
}
