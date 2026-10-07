package com.shivanshu.personal_finance_manager.integration;

import com.jayway.jsonpath.JsonPath;
import com.shivanshu.personal_finance_manager.dto.request.CreateTransactionRequest;
import com.shivanshu.personal_finance_manager.dto.request.UpdateTransactionRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests for transaction CRUD, filtering, and immutable date validation.
 */
class TransactionFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void transactionLifecycle_test() throws Exception {
        String username = "tx_user@example.com";
        String password = "Password123!";
        registerUser(username, password, "Tx User", "+919876543211");
        Cookie cookie = loginAndGetSessionCookie(username, password);

        // 1. Create transaction with future date returns 400
        CreateTransactionRequest futureTx = new CreateTransactionRequest(
                new BigDecimal("100.00"),
                LocalDate.now().plusDays(5),
                "Food",
                "Future Dinner"
        );
        mockMvc.perform(post("/api/transactions")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futureTx)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // 2. Create valid transaction returns 201
        CreateTransactionRequest validTx = new CreateTransactionRequest(
                new BigDecimal("50.00"),
                LocalDate.now().minusDays(1),
                "Food",
                "Groceries"
        );
        var createResult = mockMvc.perform(post("/api/transactions")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTx)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andReturn();

        Integer txId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        // 3. List transactions
        mockMvc.perform(get("/api/transactions").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)));

        // 4. Update transaction with immutable date change returns 400
        UpdateTransactionRequest changeDateReq = new UpdateTransactionRequest(
                new BigDecimal("60.00"),
                "Changed",
                null,
                LocalDate.now().minusDays(2)
        );
        mockMvc.perform(put("/api/transactions/" + txId)
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeDateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // 5. Update amount and description returns 200
        UpdateTransactionRequest validUpdate = new UpdateTransactionRequest(
                new BigDecimal("75.00"),
                "Updated Groceries",
                null,
                null
        );
        mockMvc.perform(put("/api/transactions/" + txId)
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(75.00))
                .andExpect(jsonPath("$.description").value("Updated Groceries"));

        // 6. Delete transaction returns 200
        mockMvc.perform(delete("/api/transactions/" + txId).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));

        // 7. List now empty
        mockMvc.perform(get("/api/transactions").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(0)));
    }
}
