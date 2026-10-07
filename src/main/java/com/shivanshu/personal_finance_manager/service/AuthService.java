package com.shivanshu.personal_finance_manager.service;

import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.dto.response.RegisterResponse;
import org.springframework.security.core.Authentication;

/**
 * Service contract for user registration and authentication logic.
 */
public interface AuthService {

    /**
     * Registers a new user with email uniqueness validation and password hashing.
     *
     * @param request The registration details
     * @return RegisterResponse containing success message and assigned user ID
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Validates credentials and returns an authenticated Authentication token.
     *
     * @param request The login credentials
     * @return Validated Authentication instance
     */
    Authentication authenticate(LoginRequest request);
}
