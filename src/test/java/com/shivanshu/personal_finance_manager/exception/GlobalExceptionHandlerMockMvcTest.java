package com.shivanshu.personal_finance_manager.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GlobalExceptionHandlerMockMvcTest.TestFaultyController.class)
class GlobalExceptionHandlerMockMvcTest {

    @TestConfiguration
    @RestController
    static class TestFaultyController {
        @GetMapping("/api/test/generic-failure")
        public String triggerGenericException() {
            throw new RuntimeException("Simulated unexpected crash");
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Cookie authCookie;

    @BeforeEach
    void setUp() throws Exception {
        String email = "gehuser_" + System.nanoTime() + "@example.com";
        RegisterRequest registerReq = new RegisterRequest(email, "Password123!", "GEH User", "+1234567890");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest(email, "Password123!");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        authCookie = result.getResponse().getCookie("accessToken");
    }

    @Test
    @DisplayName("400 Validation Error: Missing/invalid fields produce structured ErrorResponse with details")
    void testValidationErrorReturns400() throws Exception {
        String invalidTransactionJson = "{\"amount\": -10.00, \"date\": \"2024-01-01\", \"category\": \"\"}";

        mockMvc.perform(post("/api/transactions")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTransactionJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details", not(empty())));
    }

    @Test
    @DisplayName("400 Malformed JSON: Broken JSON syntax returns structured ErrorResponse")
    void testMalformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json-body"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    @Test
    @DisplayName("400 Type Mismatch: Non-numeric path or query param returns structured ErrorResponse")
    void testTypeMismatchReturns400() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .cookie(authCookie)
                        .param("categoryId", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid parameter: categoryId"));
    }

    @Test
    @DisplayName("404 Unknown Path: Non-existent endpoint URL returns structured ErrorResponse")
    void testNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/unknown-nonexistent-route-xyz")
                        .cookie(authCookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    @DisplayName("405 Wrong Method: Disallowed HTTP method returns structured ErrorResponse")
    void testMethodNotSupportedReturns405() throws Exception {
        mockMvc.perform(post("/api/health")
                        .cookie(authCookie))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.message", containsString("Request method 'POST' is not supported")));
    }

    @Test
    @DisplayName("409 Conflict: Duplicate registration or constraint conflict returns structured ErrorResponse")
    void testConflictReturns409() throws Exception {
        String existingEmail = "conflict_" + System.nanoTime() + "@example.com";
        RegisterRequest reg = new RegisterRequest(existingEmail, "Password123!", "User A", "+1234567890");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Duplicate registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    @DisplayName("401 Unauthorized: Accessing protected endpoints unauthenticated returns structured ErrorResponse")
    void testUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @DisplayName("403 Forbidden: Attempting to delete default category returns structured ErrorResponse")
    void testForbiddenDefaultCategoryDeleteReturns403() throws Exception {
        mockMvc.perform(delete("/api/categories/Salary")
                        .cookie(authCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Default category cannot be deleted"));
    }

    @Test
    @DisplayName("500 Generic Server Error: Unhandled unexpected exception returns structured ErrorResponse")
    void testGenericExceptionReturns500() throws Exception {
        mockMvc.perform(get("/api/test/generic-failure")
                        .cookie(authCookie))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
