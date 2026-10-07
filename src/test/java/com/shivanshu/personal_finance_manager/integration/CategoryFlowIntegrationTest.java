package com.shivanshu.personal_finance_manager.integration;

import com.shivanshu.personal_finance_manager.dto.request.CreateCategoryRequest;
import com.shivanshu.personal_finance_manager.entity.CategoryType;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests for category endpoints.
 */
class CategoryFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void categoryLifecycle_test() throws Exception {
        String username = "cat_user@example.com";
        String password = "Password123!";
        registerUser(username, password, "Category User", "+919876543210");
        Cookie cookie = loginAndGetSessionCookie(username, password);

        // 1. Verify defaults are listed
        mockMvc.perform(get("/api/categories").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[*].name", hasItem("Salary")))
                .andExpect(jsonPath("$.categories[*].name", hasItem("Food")));

        // 2. Create custom category
        CreateCategoryRequest newCat = new CreateCategoryRequest("Freelance", CategoryType.INCOME);
        mockMvc.perform(post("/api/categories")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Freelance"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.isCustom").value(true));

        // 3. Duplicate custom category returns 409
        mockMvc.perform(post("/api/categories")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCat)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // 4. Attempt to delete default category returns 403
        mockMvc.perform(delete("/api/categories/Salary").cookie(cookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // 5. Delete custom category returns 200
        mockMvc.perform(delete("/api/categories/Freelance").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        // 6. Delete again returns 404
        mockMvc.perform(delete("/api/categories/Freelance").cookie(cookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
