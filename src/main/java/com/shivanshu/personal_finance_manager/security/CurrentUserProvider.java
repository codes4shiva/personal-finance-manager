package com.shivanshu.personal_finance_manager.security;

import com.shivanshu.personal_finance_manager.entity.User;

/**
 * Interface abstracting retrieval of the currently authenticated user.
 */
public interface CurrentUserProvider {

    /**
     * Retrieves the ID of the currently authenticated user.
     *
     * @return User ID
     */
    Long getCurrentUserId();

    /**
     * Retrieves the UserDetails of the currently authenticated user.
     *
     * @return AppUserDetails instance
     */
    AppUserDetails getCurrentUserDetails();

    /**
     * Retrieves the JPA User entity of the currently authenticated user.
     *
     * @return User entity
     */
    User getCurrentUserEntity();
}
