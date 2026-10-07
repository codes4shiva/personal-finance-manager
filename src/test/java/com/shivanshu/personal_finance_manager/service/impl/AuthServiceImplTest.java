package com.shivanshu.personal_finance_manager.service.impl;

import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.dto.response.RegisterResponse;
import com.shivanshu.personal_finance_manager.entity.User;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, authenticationManager);
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("test@example.com", "Password123!", "Test User", "+1234567890");

        when(userRepository.existsByUsernameIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");

        User savedUser = new User("test@example.com", "encodedPassword", "Test User", "+1234567890");
        savedUser.setId(100L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(100L, response.userId());
        assertEquals("User registered successfully", response.message());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("test@example.com", captor.getValue().getUsername());
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        RegisterRequest request = new RegisterRequest("duplicate@example.com", "Password123!", "Test User", "+1234567890");

        when(userRepository.existsByUsernameIgnoreCase("duplicate@example.com")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> authService.register(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("Username already exists", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_validCredentials_returnsAuthentication() {
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");
        Authentication expectedAuth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(expectedAuth);

        Authentication actualAuth = authService.authenticate(request);
        assertEquals(expectedAuth, actualAuth);
    }

    @Test
    void authenticate_blankCredentials_throwsUnauthorized() {
        LoginRequest blankUser = new LoginRequest("", "Password123!");
        ApiException ex1 = assertThrows(ApiException.class, () -> authService.authenticate(blankUser));
        assertEquals(HttpStatus.UNAUTHORIZED, ex1.getStatus());

        LoginRequest blankPass = new LoginRequest("test@example.com", "   ");
        ApiException ex2 = assertThrows(ApiException.class, () -> authService.authenticate(blankPass));
        assertEquals(HttpStatus.UNAUTHORIZED, ex2.getStatus());
    }

    @Test
    void authenticate_invalidCredentials_throwsUnauthorized() {
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ApiException ex = assertThrows(ApiException.class, () -> authService.authenticate(request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("Invalid username or password", ex.getMessage());
    }
}
