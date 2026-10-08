package com.shivanshu.personal_finance_manager.controller;

import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.dto.response.AuthMessageResponse;
import com.shivanshu.personal_finance_manager.dto.response.RegisterResponse;
import com.shivanshu.personal_finance_manager.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Controller exposing endpoints for user registration, login, and logout.
 * Authentication uses JWT tokens stored in HttpOnly cookies.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final boolean cookieSecure;

    public AuthController(
            AuthService authService,
            @Value("${app.cookie.secure:false}") boolean cookieSecure
    ) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
    }

    /**
     * Registers a new user account.
     *
     * @param request Validated registration parameters
     * @return 201 Created with user ID and confirmation message
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticates credentials and sets accessToken and refreshToken HttpOnly cookies.
     *
     * @param request  Validated login credentials
     * @param response HTTP servlet response for adding Set-Cookie headers
     * @return 200 OK with login confirmation message
     */
    @PostMapping("/login")
    public ResponseEntity<AuthMessageResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        String[] tokens = authService.authenticate(request); // [0]=access, [1]=refresh

        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("accessToken", tokens[0], "/", Duration.ofHours(1)).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("refreshToken", tokens[1], "/api/auth", Duration.ofDays(10)).toString());

        return ResponseEntity.ok(new AuthMessageResponse("Login successful"));
    }

    /**
     * Clears authentication cookies upon user logout.
     *
     * @param response HTTP servlet response for expiring cookies
     * @return 200 OK with logout confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthMessageResponse> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("accessToken", "", "/", Duration.ZERO).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("refreshToken", "", "/api/auth", Duration.ZERO).toString());

        return ResponseEntity.ok(new AuthMessageResponse("Logout successful"));
    }

    private ResponseCookie buildCookie(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}
