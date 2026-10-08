package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.UserDTO;
import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.dto.response.RegisterResponse;
import com.shivanshu.personal_finance_manager.entity.UserEntity;
import com.shivanshu.personal_finance_manager.entity.UserPrincipal;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.repository.UserRepository;
import com.shivanshu.personal_finance_manager.security.AppUserDetails;
import com.shivanshu.personal_finance_manager.security.JwtService;
import com.shivanshu.personal_finance_manager.service.AuthService;
import lombok.AllArgsConstructor;
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

//    @Override
//    @Transactional(readOnly = true)
//    public Authentication authenticate(LoginRequest request) {
//        if (request.getUsername() == null || request.getUsername().isBlank()
//                || request.getPassword() == null || request.getPassword().isBlank()) {
//            throw ApiException.unauthorized("Invalid username or password");
//        }
//
//        String normalizedEmail = request.getUsername().trim().toLowerCase();
//        try {
//            return authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
//            );
//        } catch (AuthenticationException ex) {
//            throw ApiException.unauthorized("Invalid username or password");
//        }
//    }

    public String[] authenticate(LoginRequest loginRequestDTO) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequestDTO.getUsername(),
                                loginRequestDTO.getPassword()
                        )
                );

        AppUserDetails userDetails =
                (AppUserDetails) authentication.getPrincipal();

        UserEntity user = userDetails.getUserEntity();

        String[] token = new String[2];
        token[0] = jwtService.generateAccessToken(user.getUsername());
        token[1] = jwtService.generateRefreshToken(user.getUsername());

        return token;
    }
}
