package com.shivanshu.personal_finance_manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
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
class TransactionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Cookie userACookie;
    private Cookie userBCookie;

    @BeforeEach
    void setUp() throws Exception {
        userACookie = registerAndLogin("txuserA_" + System.nanoTime() + "@example.com", "Password123!");
        userBCookie = registerAndLogin("txuserB_" + System.nanoTime() + "@example.com", "Password123!");
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
    @DisplayName("Happy path: create, get, partial update, delete transaction")
    void testTransactionCrudLifecycle() throws Exception {
        CreateTransactionRequest createReq = new CreateTransactionRequest(
                new BigDecimal("50000.00"),
                LocalDate.of(2024, 1, 15),
                "Salary",
                "January Salary"
        );

        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.amount").value(50000.00))
                .andExpect(jsonPath("$.date").value("2024-01-15"))
                .andExpect(jsonPath("$.category").value("Salary"))
                .andExpect(jsonPath("$.description").value("January Salary"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long txId = jsonNode.get("id").asLong();

        // GET transactions
        mockMvc.perform(get("/api/transactions")
                        .cookie(userACookie)
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.transactions[0].id").value(txId));

        // PUT partial update
        UpdateTransactionRequest updateReq = new UpdateTransactionRequest(
                new BigDecimal("60000.00"),
                "Updated January Salary",
                null,
                null
        );

        mockMvc.perform(put("/api/transactions/" + txId)
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(txId))
                .andExpect(jsonPath("$.amount").value(60000.00))
                .andExpect(jsonPath("$.description").value("Updated January Salary"))
                .andExpect(jsonPath("$.date").value("2024-01-15"));

        // DELETE transaction
        mockMvc.perform(delete("/api/transactions/" + txId)
                        .cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));

        // GET after delete
        mockMvc.perform(get("/api/transactions")
                        .cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(0)));
    }

    @Test
    @DisplayName("Error path: create transaction with unknown category returns 400")
    void testCreateTransactionUnknownCategory() throws Exception {
        CreateTransactionRequest createReq = new CreateTransactionRequest(
                new BigDecimal("100.00"),
                LocalDate.of(2024, 1, 15),
                "UnknownCategoryXYZ",
                "Desc"
        );

        mockMvc.perform(post("/api/transactions")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Transaction query filters: none, startDate only, endDate only, categoryId only, all combined, and user isolation")
    void testTransactionQueryFilterCombinations() throws Exception {
        // Find food category id from GET /api/categories
        MvcResult catResult = mockMvc.perform(get("/api/categories").cookie(userACookie))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode cats = objectMapper.readTree(catResult.getResponse().getContentAsString()).get("categories");
        // We know Food and Salary exist as default categories
        CreateTransactionRequest t1 = new CreateTransactionRequest(
                new BigDecimal("100.00"), LocalDate.of(2024, 2, 10), "Food", "Dinner"
        );
        CreateTransactionRequest t2 = new CreateTransactionRequest(
                new BigDecimal("200.00"), LocalDate.of(2024, 2, 20), "Food", "Lunch"
        );
        CreateTransactionRequest t3 = new CreateTransactionRequest(
                new BigDecimal("5000.00"), LocalDate.of(2024, 2, 25), "Salary", "Bonus"
        );
        // Create user B transaction (must never appear for user A)
        CreateTransactionRequest tUserB = new CreateTransactionRequest(
                new BigDecimal("999.00"), LocalDate.of(2024, 2, 15), "Food", "User B Food"
        );

        mockMvc.perform(post("/api/transactions").cookie(userACookie)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(t1))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/transactions").cookie(userACookie)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(t2))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/transactions").cookie(userACookie)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(t3))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/transactions").cookie(userBCookie)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tUserB))).andExpect(status().isCreated());

        // 1. None (all transactions of user A)
        mockMvc.perform(get("/api/transactions").cookie(userACookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(3)))
                .andExpect(jsonPath("$.transactions[*].description", not(hasItem("User B Food"))));

        // 2. startDate only
        mockMvc.perform(get("/api/transactions").cookie(userACookie)
                        .param("startDate", "2024-02-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.transactions[*].description", not(hasItem("Dinner"))));

        // 3. endDate only
        mockMvc.perform(get("/api/transactions").cookie(userACookie)
                        .param("endDate", "2024-02-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.transactions[0].description").value("Dinner"));

        // 4. category filter
        mockMvc.perform(get("/api/transactions").cookie(userACookie)
                        .param("category", "Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.transactions[*].category", everyItem(is("Food"))));

        // 5. All combined: startDate, endDate, category, type
        mockMvc.perform(get("/api/transactions").cookie(userACookie)
                        .param("startDate", "2024-02-01")
                        .param("endDate", "2024-02-28")
                        .param("category", "Salary")
                        .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.transactions[0].description").value("Bonus"));

        // 6. Non-numeric categoryId -> 400
        mockMvc.perform(get("/api/transactions").cookie(userACookie)
                        .param("categoryId", "non-numeric"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("categoryId")));

        // 7. startDate after endDate -> 400
        mockMvc.perform(get("/api/transactions").cookie(userACookie)
                        .param("startDate", "2024-03-01")
                        .param("endDate", "2024-02-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("User isolation: User B cannot see, update, or delete User A's transaction (404)")
    void testUserIsolationOnTransactions() throws Exception {
        CreateTransactionRequest createReq = new CreateTransactionRequest(
                new BigDecimal("250.00"),
                LocalDate.of(2024, 1, 10),
                "Food",
                "Groceries"
        );

        MvcResult result = mockMvc.perform(post("/api/transactions")
                        .cookie(userACookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long txId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // User B cannot see User A's transaction
        mockMvc.perform(get("/api/transactions")
                        .cookie(userBCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(0)));

        // User B updating User A's transaction returns 404
        UpdateTransactionRequest updateReq = new UpdateTransactionRequest(
                new BigDecimal("300.00"),
                "Hacked",
                null,
                null
        );
        mockMvc.perform(put("/api/transactions/" + txId)
                        .cookie(userBCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // User B deleting User A's transaction returns 404
        mockMvc.perform(delete("/api/transactions/" + txId)
                        .cookie(userBCookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}

