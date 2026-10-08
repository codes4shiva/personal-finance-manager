package com.shivanshu.personal_finance_manager.security;

import com.shivanshu.personal_finance_manager.entity.UserEntity;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implementation of CurrentUserProvider obtaining user information
 * from the Spring SecurityContextHolder.
 */
@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;

    public SecurityCurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    @Override
    public AppUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            throw ApiException.unauthorized("Authentication required");
        }
        return userDetails;
    }

    @Override
    public UserEntity getCurrentUserEntity() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));
    }
}
