package com.shivanshu.personal_finance_manager.integration;

import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests for monthly and yearly financial reporting endpoints.
 */
class ReportFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void reportEndpoints_test() throws Exception {
        String username = "report_user@example.com";
        String password = "Password123!";
        registerUser(username, password, "Report User", "+919876543214");
        Cookie cookie = loginAndGetSessionCookie(username, password);

        // Add income: 5000.00
        CreateTransactionRequest income = new CreateTransactionRequest(
                new BigDecimal("5000.00"),
                LocalDate.of(2026, 3, 10),
                "Salary",
                "March Salary"
        );
        mockMvc.perform(post("/api/transactions")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(income)))
                .andExpect(status().isCreated());

        // Add expense: 1200.00
        CreateTransactionRequest expense = new CreateTransactionRequest(
                new BigDecimal("1200.00"),
                LocalDate.of(2026, 3, 15),
                "Rent",
                "March Rent"
        );
        mockMvc.perform(post("/api/transactions")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated());

        // 1. Monthly report for 2026/03
        mockMvc.perform(get("/api/reports/monthly/2026/3").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(3))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.totalIncome.Salary").value(5000.00))
                .andExpect(jsonPath("$.totalExpenses.Rent").value(1200.00))
                .andExpect(jsonPath("$.netSavings").value(3800.00));

        // 2. Monthly report with invalid month returns 400
        mockMvc.perform(get("/api/reports/monthly/2026/13").cookie(cookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // 3. Yearly report for 2026
        mockMvc.perform(get("/api/reports/yearly/2026").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.totalIncome.Salary").value(5000.00))
                .andExpect(jsonPath("$.totalExpenses.Rent").value(1200.00))
                .andExpect(jsonPath("$.netSavings").value(3800.00));

        // 4. Empty period returns empty maps and 0.00 netSavings
        mockMvc.perform(get("/api/reports/monthly/2026/1").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.netSavings").value(0.00));
    }
}
