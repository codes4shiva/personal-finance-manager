package com.shivanshu.personal_finance_manager.controller;

import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.dto.response.AuthMessageResponse;
import com.shivanshu.personal_finance_manager.dto.response.RegisterResponse;
import com.shivanshu.personal_finance_manager.exception.ApiException;
import com.shivanshu.personal_finance_manager.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing endpoints for user registration, login, and logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    public AuthController(
            AuthService authService,
            SecurityContextRepository securityContextRepository
    ) {
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Registers a new user account.
     *
     * @param request Validated registration parameters
     * @return 201 Created with user ID and confirmation message
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates an existing user and establishes an HTTP session.
     *
     * @param request      Login credentials
     * @param httpRequest  Current HTTP servlet request
     * @param httpResponse Current HTTP servlet response
     * @return 200 OK with success message and session cookie
     */
    @PostMapping("/login")
    public ResponseEntity<AuthMessageResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Authentication authentication = authService.authenticate(request);

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return ResponseEntity.ok(new AuthMessageResponse("Login successful"));
    }

    /**
     * Terminates the current authenticated session.
     *
     * @param httpRequest  Current HTTP servlet request
     * @param httpResponse Current HTTP servlet response
     * @return 200 OK with logout confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthMessageResponse> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            throw ApiException.unauthorized("Authentication required");
        }
        session.invalidate();

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return ResponseEntity.ok(new AuthMessageResponse("Logout successful"));
    }
}
