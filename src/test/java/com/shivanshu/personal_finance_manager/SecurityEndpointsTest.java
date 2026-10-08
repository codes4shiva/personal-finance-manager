package com.shivanshu.personal_finance_manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated POST /api/transactions returns 401")
    void testPostTransactionsUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100.00, \"date\": \"2024-01-15\", \"category\": \"Salary\", \"description\": \"Test\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Unauthenticated GET /api/transactions returns 401")
    void testGetTransactionsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated PUT /api/transactions/1 returns 401")
    void testPutTransactionsUnauthenticated() throws Exception {
        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 200.00}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated DELETE /api/transactions/1 returns 401")
    void testDeleteTransactionsUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated GET /api/categories returns 401")
    void testGetCategoriesUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated POST /api/categories returns 401")
    void testPostCategoriesUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"FreelanceTest\", \"type\": \"INCOME\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated DELETE /api/categories/Food returns 401")
    void testDeleteCategoriesUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/categories/Food"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated POST /api/goals returns 401")
    void testPostGoalsUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goalName\": \"Test\", \"targetAmount\": 500.00, \"targetDate\": \"2026-05-01\", \"startDate\": \"2025-01-01\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated GET /api/goals returns 401")
    void testGetGoalsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated GET /api/goals/1 returns 401")
    void testGetGoalByIdUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated PUT /api/goals/1 returns 401")
    void testPutGoalUnauthenticated() throws Exception {
        mockMvc.perform(put("/api/goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetAmount\": 600.00}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated DELETE /api/goals/1 returns 401")
    void testDeleteGoalUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/goals/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated GET /api/reports/monthly/2024/1 returns 401")
    void testGetMonthlyReportUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/2024/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated GET /api/reports/yearly/2024 returns 401")
    void testGetYearlyReportUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/reports/yearly/2024"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated POST /api/auth/logout returns 401")
    void testLogoutUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
