package com.shivanshu.personal_finance_manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivanshu.personal_finance_manager.dto.request.CreateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateTransactionRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EdgeCasesApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Cookie authCookie;

    @BeforeEach
    void setUp() throws Exception {
        String email = "edge_" + System.nanoTime() + "@example.com";
        RegisterRequest reg = new RegisterRequest(email, "Password123!", "Edge User", "+1234567890");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest(email, "Password123!");
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        authCookie = res.getResponse().getCookie("accessToken");
    }

    @Test
    @DisplayName("Report Edge Case: empty month returns 200, empty maps, and netSavings 0.00")
    void testEmptyMonthReport() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/2099/1")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2099))
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.totalIncome").isMap())
                .andExpect(jsonPath("$.totalIncome", aMapWithSize(0)))
                .andExpect(jsonPath("$.totalExpenses").isMap())
                .andExpect(jsonPath("$.totalExpenses", aMapWithSize(0)))
                .andExpect(jsonPath("$.netSavings").value(0.00));
    }

    @Test
    @DisplayName("Report Edge Case: month 0 and 13 return 400 Bad Request")
    void testInvalidMonthRangeReturns400() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/2024/0")
                        .cookie(authCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/api/reports/monthly/2024/13")
                        .cookie(authCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Goal Edge Case: progress above 100% and remainingAmount never negative (floored at 0.00)")
    void testGoalProgressAbove100Percent() throws Exception {
        CreateGoalRequest goalReq = new CreateGoalRequest(
                "Gadget Fund",
                new BigDecimal("1000.00"),
                LocalDate.of(2025, 12, 31),
                LocalDate.of(2024, 1, 1)
        );
        MvcResult goalRes = mockMvc.perform(post("/api/goals")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long goalId = objectMapper.readTree(goalRes.getResponse().getContentAsString()).get("id").asLong();

        // Income of 2500 against goal target of 1000
        CreateTransactionRequest incomeReq = new CreateTransactionRequest(
                new BigDecimal("2500.00"),
                LocalDate.of(2024, 2, 1),
                "Salary",
                "Large Bonus"
        );
        mockMvc.perform(post("/api/transactions")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incomeReq)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/goals/" + goalId)
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentProgress").value(2500.00))
                .andExpect(jsonPath("$.progressPercentage").value(250.00))
                .andExpect(jsonPath("$.remainingAmount").value(0.00));
    }

    @Test
    @DisplayName("Partial Update Edge Case: PUT /api/transactions/{id} preserves untouched fields")
    void testPartialPutTransactionPreservesUntouchedFields() throws Exception {
        CreateTransactionRequest createReq = new CreateTransactionRequest(
                new BigDecimal("500.00"),
                LocalDate.of(2024, 4, 10),
                "Food",
                "Dinner with friends"
        );
        MvcResult createRes = mockMvc.perform(post("/api/transactions")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long txId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        // Only update amount; description, date, and category are omitted
        UpdateTransactionRequest updateReq = new UpdateTransactionRequest(
                new BigDecimal("750.00"),
                null,
                null,
                null
        );
        mockMvc.perform(put("/api/transactions/" + txId)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(txId))
                .andExpect(jsonPath("$.amount").value(750.00))
                .andExpect(jsonPath("$.date").value("2024-04-10"))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.description").value("Dinner with friends"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("Partial Update Edge Case: PUT /api/goals/{id} preserves untouched fields")
    void testPartialPutGoalPreservesUntouchedFields() throws Exception {
        CreateGoalRequest createReq = new CreateGoalRequest(
                "Vacation",
                new BigDecimal("2000.00"),
                LocalDate.of(2025, 12, 31),
                LocalDate.of(2024, 1, 1)
        );
        MvcResult createRes = mockMvc.perform(post("/api/goals")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long goalId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        // Only update targetAmount; goalName, startDate, targetDate are omitted
        UpdateGoalRequest updateReq = new UpdateGoalRequest(
                null,
                new BigDecimal("3500.00"),
                null,
                null
        );
        mockMvc.perform(put("/api/goals/" + goalId)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(goalId))
                .andExpect(jsonPath("$.goalName").value("Vacation"))
                .andExpect(jsonPath("$.targetAmount").value(3500.00))
                .andExpect(jsonPath("$.startDate").value("2024-01-01"))
                .andExpect(jsonPath("$.targetDate").value("2025-12-31"));
    }
}
