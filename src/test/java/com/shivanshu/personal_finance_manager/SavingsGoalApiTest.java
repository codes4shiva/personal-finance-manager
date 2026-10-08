package com.shivanshu.personal_finance_manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivanshu.personal_finance_manager.dto.request.CreateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateGoalRequest;
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
class SavingsGoalApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Cookie userACookie;
    private Cookie userBCookie;

    @BeforeEach
    void setUp() throws Exception {
        userACookie = registerAndLogin("goaluserA_" + System.nanoTime() + "@example.com", "Password123!");
        userBCookie = registerAndLogin("goaluserB_" + System.nanoTime() + "@example.com", "Password123!");
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
    @DisplayName("Happy path: create goal with computed progress, get, partial update, delete")
    void testGoalLifecycleWithProgressComputation() throws Exception {
        // Create an income transaction for User A
        CreateTransactionRequest createTx = new CreateTransactionRequest(
                new BigDecimal("1000.00"),
                LocalDate.of(2025, 2, 1),
                "Salary",
                "Savings seed"
        );
        mockMvc.perform(post("/api/transactions")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTx)))
                .andExpect(status().isCreated());

        // Create goal with targetAmount=5000.00, startDate=2025-01-01, targetDate=2026-01-01
        CreateGoalRequest createGoal = new CreateGoalRequest(
                "Emergency Fund",
                new BigDecimal("5000.00"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2025, 1, 1)
        );

        MvcResult result = mockMvc.perform(post("/api/goals")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGoal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.targetAmount").value(5000.00))
                .andExpect(jsonPath("$.targetDate").value("2026-01-01"))
                .andExpect(jsonPath("$.startDate").value("2025-01-01"))
                .andExpect(jsonPath("$.currentProgress").value(1000.00))
                .andExpect(jsonPath("$.progressPercentage").value(20.00))
                .andExpect(jsonPath("$.remainingAmount").value(4000.00))
                .andReturn();

        long goalId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // GET all goals
        mockMvc.perform(get("/api/goals")
                        .cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals", hasSize(1)))
                .andExpect(jsonPath("$.goals[0].id").value(goalId));

        // GET goal by id
        mockMvc.perform(get("/api/goals/" + goalId)
                        .cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(goalId))
                .andExpect(jsonPath("$.currentProgress").value(1000.00));

        // PUT partial update: targetAmount=6000.00, targetDate=2026-02-01
        UpdateGoalRequest updateReq = new UpdateGoalRequest(
                null,
                new BigDecimal("6000.00"),
                LocalDate.of(2026, 2, 1),
                null
        );

        mockMvc.perform(put("/api/goals/" + goalId)
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(goalId))
                .andExpect(jsonPath("$.targetAmount").value(6000.00))
                .andExpect(jsonPath("$.targetDate").value("2026-02-01"))
                .andExpect(jsonPath("$.currentProgress").value(1000.00))
                .andExpect(jsonPath("$.progressPercentage").value(16.67))
                .andExpect(jsonPath("$.remainingAmount").value(5000.00));

        // DELETE goal
        mockMvc.perform(delete("/api/goals/" + goalId)
                        .cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }

    @Test
    @DisplayName("Error path: targetDate not after startDate returns 400")
    void testCreateGoalInvalidDates() throws Exception {
        CreateGoalRequest invalidDates = new CreateGoalRequest(
                "Invalid Dates",
                new BigDecimal("1000.00"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 1) // startDate after targetDate
        );

        mockMvc.perform(post("/api/goals")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDates)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Error path: non-numeric ID returns 400 Bad Request")
    void testGetGoalNonNumericId() throws Exception {
        mockMvc.perform(get("/api/goals/not-a-number")
                        .cookie(userACookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Error path: goal not found returns 404")
    void testGetGoalNotFound() throws Exception {
        mockMvc.perform(get("/api/goals/999999")
                        .cookie(userACookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("User isolation: User B accessing User A's goal returns 403 Forbidden")
    void testUserIsolationOnGoalsReturns403() throws Exception {
        CreateGoalRequest createGoal = new CreateGoalRequest(
                "Private Goal A",
                new BigDecimal("2000.00"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2025, 1, 1)
        );

        MvcResult result = mockMvc.perform(post("/api/goals")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGoal)))
                .andExpect(status().isCreated())
                .andReturn();

        long goalId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // User B cannot see it in list
        mockMvc.perform(get("/api/goals")
                        .cookie(userBCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals", empty()));

        // User B GET User A's goal -> 403 Forbidden
        mockMvc.perform(get("/api/goals/" + goalId)
                        .cookie(userBCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // User B PUT User A's goal -> 403 Forbidden
        UpdateGoalRequest updateReq = new UpdateGoalRequest(null, new BigDecimal("3000.00"), null, null);
        mockMvc.perform(put("/api/goals/" + goalId)
                        .cookie(userBCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // User B DELETE User A's goal -> 403 Forbidden
        mockMvc.perform(delete("/api/goals/" + goalId)
                        .cookie(userBCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
