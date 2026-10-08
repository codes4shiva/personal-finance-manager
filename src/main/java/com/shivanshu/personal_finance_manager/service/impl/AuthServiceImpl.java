package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.dto.response.RegisterResponse;
import com.shivanshu.personal_finance_manager.entity.UserEntity;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.repository.UserRepository;
import com.shivanshu.personal_finance_manager.security.AppUserDetails;
import com.shivanshu.personal_finance_manager.security.JwtService;
import com.shivanshu.personal_finance_manager.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of AuthService handling user registration
 * and JWT-based authentication.
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.username().trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(normalizedEmail)) {
            throw ApiException.conflict("Username already exists");
        }

        UserEntity userEntity = new UserEntity(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                request.phoneNumber().trim()
        );
        UserEntity savedUserEntity = userRepository.save(userEntity);

        return new RegisterResponse("User registered successfully", savedUserEntity.getId());
    }

    @Override
    public String[] authenticate(LoginRequest loginRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getUsername(),
                        loginRequestDTO.getPassword()
                )
        );

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        UserEntity user = userDetails.getUserEntity();

        String[] tokens = new String[2];
        tokens[0] = jwtService.generateAccessToken(user.getUsername());
        tokens[1] = jwtService.generateRefreshToken(user.getUsername());

        return tokens;
    }
}
