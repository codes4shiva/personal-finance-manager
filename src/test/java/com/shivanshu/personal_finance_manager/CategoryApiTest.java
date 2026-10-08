package com.shivanshu.personal_finance_manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivanshu.personal_finance_manager.dto.request.CreateCategoryRequest;
import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
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
class CategoryApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Cookie userACookie;
    private Cookie userBCookie;

    @BeforeEach
    void setUp() throws Exception {
        userACookie = registerAndLogin("catuserA_" + System.nanoTime() + "@example.com", "Password123!");
        userBCookie = registerAndLogin("catuserB_" + System.nanoTime() + "@example.com", "Password123!");
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
    @DisplayName("Happy path: list defaults, create custom category, delete custom category")
    void testCategoryCrudLifecycle() throws Exception {
        // List categories (includes defaults like Salary, Freelance, Food)
        mockMvc.perform(get("/api/categories")
                        .cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", not(empty())))
                .andExpect(jsonPath("$.categories[?(@.name == 'Salary')].isCustom").value(hasItem(false)));

        // Create custom category
        CreateCategoryRequest createReq = new CreateCategoryRequest("CustomConsulting", CategoryType.INCOME);
        mockMvc.perform(post("/api/categories")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("CustomConsulting"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.isCustom").value(true));

        // Delete custom category
        mockMvc.perform(delete("/api/categories/CustomConsulting")
                        .cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    @DisplayName("Error path: duplicate category name returns 409 Conflict")
    void testDuplicateCategory() throws Exception {
        // Trying to create a category that already exists as default
        CreateCategoryRequest duplicateDefault = new CreateCategoryRequest("Salary", CategoryType.INCOME);
        mockMvc.perform(post("/api/categories")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateDefault)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Error path: delete default category returns 403 Forbidden")
    void testDeleteDefaultCategory() throws Exception {
        mockMvc.perform(delete("/api/categories/Food")
                        .cookie(userACookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Error path: delete category with active transactions returns 400 Bad Request")
    void testDeleteCategoryWithTransactions() throws Exception {
        // Create custom category
        String catName = "ActiveCat_" + System.nanoTime();
        CreateCategoryRequest createCat = new CreateCategoryRequest(catName, CategoryType.EXPENSE);
        mockMvc.perform(post("/api/categories")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCat)))
                .andExpect(status().isCreated());

        // Create transaction using it
        CreateTransactionRequest createTx = new CreateTransactionRequest(
                new BigDecimal("50.00"),
                LocalDate.of(2024, 1, 10),
                catName,
                "Used"
        );
        mockMvc.perform(post("/api/transactions")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTx)))
                .andExpect(status().isCreated());

        // Attempt delete
        mockMvc.perform(delete("/api/categories/" + catName)
                        .cookie(userACookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("associated with transactions")));
    }

    @Test
    @DisplayName("User isolation: User B cannot see or delete User A's custom category")
    void testUserIsolationOnCategories() throws Exception {
        String catName = "PrivateCatA_" + System.nanoTime();
        CreateCategoryRequest createCat = new CreateCategoryRequest(catName, CategoryType.EXPENSE);
        mockMvc.perform(post("/api/categories")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCat)))
                .andExpect(status().isCreated());

        // User B cannot see it
        mockMvc.perform(get("/api/categories")
                        .cookie(userBCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[?(@.name == '" + catName + "')]", empty()));

        // User B deleting it gets 404
        mockMvc.perform(delete("/api/categories/" + catName)
                        .cookie(userBCookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
