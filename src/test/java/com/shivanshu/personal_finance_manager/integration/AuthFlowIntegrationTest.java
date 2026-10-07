package com.shivanshu.personal_finance_manager.integration;

import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests for registration, login, session cookies, and logout.
 */
class AuthFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void authFlow_registerLoginAccessLogout() throws Exception {
        String email = "john.doe@example.com";
        String password = "StrongPassword123";

        // 1. Register new user
        RegisterRequest registerReq = new RegisterRequest(email, password, "John Doe", "+1-555-123-4567");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").isNumber());

        // 2. Duplicate registration returns 409
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // 3. Login with wrong password returns 401
        LoginRequest badLogin = new LoginRequest(email, "WrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        // 4. Access protected endpoint without session returns 401
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        // 5. Successful login sets session cookie
        LoginRequest validLogin = new LoginRequest(email, password);
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JSESSIONID"))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        Cookie sessionCookie = loginResult.getResponse().getCookie("JSESSIONID");

        // 6. Access protected endpoint with cookie succeeds
        mockMvc.perform(get("/api/categories")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray());

        // 7. Logout invalidates session
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));

        // 8. Access with old session cookie returns 401
        mockMvc.perform(get("/api/categories")
                        .cookie(sessionCookie))
                .andExpect(status().isUnauthorized());
    }
}
