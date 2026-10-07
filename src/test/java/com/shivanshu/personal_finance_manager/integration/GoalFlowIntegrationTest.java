package com.shivanshu.personal_finance_manager.integration;

import com.jayway.jsonpath.JsonPath;
import com.shivanshu.personal_finance_manager.dto.request.CreateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateGoalRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests for savings goal creation, progress calculation, and authorization isolation.
 */
class GoalFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void goalLifecycle_andDataIsolation_test() throws Exception {
        // User 1
        String user1 = "goal_user1@example.com";
        String pass1 = "Password123!";
        registerUser(user1, pass1, "Goal User 1", "+919876543212");
        Cookie cookie1 = loginAndGetSessionCookie(user1, pass1);

        // Add income transaction for user 1
        CreateTransactionRequest incomeTx = new CreateTransactionRequest(
                new BigDecimal("2000.00"),
                LocalDate.now(),
                "Salary",
                "Monthly Salary"
        );
        mockMvc.perform(post("/api/transactions")
                        .cookie(cookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incomeTx)))
                .andExpect(status().isCreated());

        // 1. Create savings goal with target 10000.00
        CreateGoalRequest createGoalReq = new CreateGoalRequest(
                "Emergency Fund",
                new BigDecimal("10000.00"),
                LocalDate.now().plusMonths(6),
                LocalDate.now().minusDays(1)
        );
        var goalResult = mockMvc.perform(post("/api/goals")
                        .cookie(cookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGoalReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.currentProgress").value(2000.00))
                .andExpect(jsonPath("$.progressPercentage").value(20.00))
                .andExpect(jsonPath("$.remainingAmount").value(8000.00))
                .andReturn();

        Integer goalId = JsonPath.read(goalResult.getResponse().getContentAsString(), "$.id");

        // 2. User 2 logs in and attempts to access User 1's goal -> 403 Forbidden
        String user2 = "goal_user2@example.com";
        String pass2 = "Password123!";
        registerUser(user2, pass2, "Goal User 2", "+919876543213");
        Cookie cookie2 = loginAndGetSessionCookie(user2, pass2);

        mockMvc.perform(get("/api/goals/" + goalId).cookie(cookie2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // 3. User 1 updates targetAmount to 20000.00
        UpdateGoalRequest updateReq = new UpdateGoalRequest(new BigDecimal("20000.00"), null);
        mockMvc.perform(put("/api/goals/" + goalId)
                        .cookie(cookie1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetAmount").value(20000.00))
                .andExpect(jsonPath("$.progressPercentage").value(10.00))
                .andExpect(jsonPath("$.remainingAmount").value(18000.00));

        // 4. User 1 deletes goal -> 200 OK
        mockMvc.perform(delete("/api/goals/" + goalId).cookie(cookie1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));

        // 5. Query after deletion -> 404 Not Found
        mockMvc.perform(get("/api/goals/" + goalId).cookie(cookie1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
