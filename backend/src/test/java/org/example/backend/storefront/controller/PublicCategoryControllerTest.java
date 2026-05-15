package org.example.backend.storefront.controller;

import org.example.backend.product.model.ProductCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PublicCategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    // ─── GET /api/public/categories ───────────────────────────────────────────

    @Test
    void getCategories_returnsAllCategories() throws Exception {
        int expectedCount = ProductCategory.values().length;

        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expectedCount));
    }

    @Test
    void getCategories_firstEntryHasValueAndLabel() throws Exception {
        ProductCategory first = ProductCategory.values()[0];

        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value(first.name()))
                .andExpect(jsonPath("$[0].label").value(first.getLabel()));
    }

    @Test
    void getCategories_lastEntryIsSonstiges() throws Exception {
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.value == 'SONSTIGES')].label").value("Sonstiges"));
    }

    @Test
    void getCategories_isPubliclyAccessible_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk());
    }
}

