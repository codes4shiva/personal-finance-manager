package com.shivanshu.personal_finance_manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Cookie userACookie;
    private Cookie userBCookie;

    @BeforeEach
    void setUp() throws Exception {
        userACookie = registerAndLogin("reportuserA_" + System.nanoTime() + "@example.com", "Password123!");
        userBCookie = registerAndLogin("reportuserB_" + System.nanoTime() + "@example.com", "Password123!");
    }

    private Cookie registerAndLogin(String email, String password) throws Exception {
        RegisterRequest registerReq = new RegisterRequest(email, password, "User", "+1234567890");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("accessToken");
    }

    @Test
    @DisplayName("Happy path: monthly and yearly report aggregation")
    void testMonthlyAndYearlyReports() throws Exception {
        // Seed transactions for User A
        addTransaction(userACookie, "3000.00", "2024-01-10", "Salary", "Jan Salary");
        addTransaction(userACookie, "500.00", "2024-01-15", "Freelance", "Side project");
        addTransaction(userACookie, "400.00", "2024-01-12", "Food", "Groceries");
        addTransaction(userACookie, "1200.00", "2024-01-05", "Rent", "Apartment rent");
        addTransaction(userACookie, "200.00", "2024-01-20", "Transportation", "Bus pass");

        // Monthly report for 2024/1
        mockMvc.perform(get("/api/reports/monthly/2024/1")
                        .cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome.Salary").value(3000.00))
                .andExpect(jsonPath("$.totalIncome.Freelance").value(500.00))
                .andExpect(jsonPath("$.totalExpenses.Food").value(400.00))
                .andExpect(jsonPath("$.totalExpenses.Rent").value(1200.00))
                .andExpect(jsonPath("$.totalExpenses.Transportation").value(200.00))
                .andExpect(jsonPath("$.netSavings").value(1700.00));

        // Yearly report for 2024
        mockMvc.perform(get("/api/reports/yearly/2024")
                        .cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome.Salary").value(3000.00))
                .andExpect(jsonPath("$.totalIncome.Freelance").value(500.00))
                .andExpect(jsonPath("$.totalExpenses.Food").value(400.00))
                .andExpect(jsonPath("$.totalExpenses.Rent").value(1200.00))
                .andExpect(jsonPath("$.totalExpenses.Transportation").value(200.00))
                .andExpect(jsonPath("$.netSavings").value(1700.00));

        // Empty period for User A (e.g., month with no data) returns empty maps and netSavings 0.00
        mockMvc.perform(get("/api/reports/monthly/2024/6")
                        .cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(6))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome", is(java.util.Collections.emptyMap())))
                .andExpect(jsonPath("$.totalExpenses", is(java.util.Collections.emptyMap())))
                .andExpect(jsonPath("$.netSavings").value(0.00));
    }

    @Test
    @DisplayName("Error path: month outside 1-12 returns 400 Bad Request")
    void testMonthlyReportInvalidMonth() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/2024/13")
                        .cookie(userACookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("User isolation: User B does not see User A's report data")
    void testUserIsolationOnReports() throws Exception {
        addTransaction(userACookie, "5000.00", "2024-03-01", "Salary", "User A salary");

        // User B report should have no income
        mockMvc.perform(get("/api/reports/monthly/2024/3")
                        .cookie(userBCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome", is(java.util.Collections.emptyMap())))
                .andExpect(jsonPath("$.totalExpenses", is(java.util.Collections.emptyMap())))
                .andExpect(jsonPath("$.netSavings").value(0.00));
    }

    private void addTransaction(Cookie cookie, String amount, String date, String category, String description) throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal(amount),
                LocalDate.parse(date),
                category,
                description
        );
        mockMvc.perform(post("/api/transactions")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
