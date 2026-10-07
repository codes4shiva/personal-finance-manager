package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.dto.response.RegisterResponse;
import com.shivanshu.personal_finance_manager.entity.User;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.repository.UserRepository;
import com.shivanshu.personal_finance_manager.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of AuthService handling user registration
 * and authentication without touching HTTP or SecurityContext APIs directly.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.username().trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(normalizedEmail)) {
            throw ApiException.conflict("Username already exists");
        }

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                request.phoneNumber().trim()
        );
        User savedUser = userRepository.save(user);

        return new RegisterResponse("User registered successfully", savedUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Authentication authenticate(LoginRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw ApiException.unauthorized("Invalid username or password");
        }

        String normalizedEmail = request.username().trim().toLowerCase();
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
            );
        } catch (AuthenticationException ex) {
            throw ApiException.unauthorized("Invalid username or password");
        }
    }
}
