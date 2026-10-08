package com.shivanshu.personal_finance_manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivanshu.personal_finance_manager.dto.request.CreateCategoryRequest;
import com.shivanshu.personal_finance_manager.dto.request.CreateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateGoalRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateTransactionRequest;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
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
class OwnershipSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Cookie userACookie;
    private Cookie userBCookie;

    @BeforeEach
    void setUp() throws Exception {
        userACookie = registerAndLogin("ownerA_" + System.nanoTime() + "@example.com", "Password123!");
        userBCookie = registerAndLogin("ownerB_" + System.nanoTime() + "@example.com", "Password123!");
    }

    private Cookie registerAndLogin(String email, String password) throws Exception {
        RegisterRequest reg = new RegisterRequest(email, password, "Owner User", "+1234567890");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest(email, password);
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return res.getResponse().getCookie("accessToken");
    }

    @Test
    @DisplayName("Transaction Ownership: User B cannot read, update, or delete User A's transactions (404)")
    void testTransactionOwnershipIsolation() throws Exception {
        CreateTransactionRequest txReq = new CreateTransactionRequest(
                new BigDecimal("500.00"),
                LocalDate.of(2024, 3, 1),
                "Food",
                "User A Groceries"
        );
        MvcResult txRes = mockMvc.perform(post("/api/transactions")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long txId = objectMapper.readTree(txRes.getResponse().getContentAsString()).get("id").asLong();

        // User B listing transactions must never see User A's transaction
        mockMvc.perform(get("/api/transactions").cookie(userBCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(0)));

        // User B updating User A's transaction -> 404
        UpdateTransactionRequest updateReq = new UpdateTransactionRequest(
                new BigDecimal("600.00"), "Hacked", null, null
        );
        mockMvc.perform(put("/api/transactions/" + txId)
                        .cookie(userBCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // User B deleting User A's transaction -> 404
        mockMvc.perform(delete("/api/transactions/" + txId)
                        .cookie(userBCookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Goal Ownership: User B cannot read, update, or delete User A's savings goals (403)")
    void testGoalOwnershipIsolation() throws Exception {
        CreateGoalRequest goalReq = new CreateGoalRequest(
                "Buy Motorcycle",
                new BigDecimal("80000.00"),
                LocalDate.of(2025, 12, 31),
                LocalDate.of(2024, 1, 1)
        );
        MvcResult goalRes = mockMvc.perform(post("/api/goals")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long goalId = objectMapper.readTree(goalRes.getResponse().getContentAsString()).get("id").asLong();

        // User B cannot get User A's goal by ID -> 403
        mockMvc.perform(get("/api/goals/" + goalId)
                        .cookie(userBCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied to goal"));

        // User B cannot update User A's goal -> 403
        UpdateGoalRequest updateGoalReq = new UpdateGoalRequest(
                "Compromised Goal",
                new BigDecimal("10.00"),
                null,
                null
        );
        mockMvc.perform(put("/api/goals/" + goalId)
                        .cookie(userBCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateGoalReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // User B cannot delete User A's goal -> 403
        mockMvc.perform(delete("/api/goals/" + goalId)
                        .cookie(userBCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Category Ownership: User B cannot see or delete User A's custom category (404), and default delete -> 403")
    void testCategoryOwnershipIsolation() throws Exception {
        String customCategoryName = "PrivateCategoryA_" + System.nanoTime();
        CreateCategoryRequest createCatReq = new CreateCategoryRequest(customCategoryName, CategoryType.EXPENSE);

        mockMvc.perform(post("/api/categories")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCatReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(customCategoryName))
                .andExpect(jsonPath("$.isCustom").value(true));

        // User B listing categories does not see User A's custom category
        mockMvc.perform(get("/api/categories").cookie(userBCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[*].name", not(hasItem(customCategoryName))));

        // User B trying to delete User A's custom category returns 404
        mockMvc.perform(delete("/api/categories/" + customCategoryName)
                        .cookie(userBCookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // User A trying to delete default category returns 403
        mockMvc.perform(delete("/api/categories/Food")
                        .cookie(userACookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Default category cannot be deleted"));
    }
}
