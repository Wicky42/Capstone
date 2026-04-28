package org.example.backend.seller.controller;

import org.example.backend.common.model.Address;
import org.example.backend.seller.dto.SellerDataDto;
import org.example.backend.seller.service.SellerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SellerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SellerService sellerService;

    final SellerDataDto VALID_SELLER_DATA = new SellerDataDto(
            "Test Shop",
            new Address("Test Street", "6", "58313", "Test City", "Test Country"),
            new Address("Test Street", "6", "58313", "Test City", "Test Country"),
            "DE123456789");

    // ─── GET /api/seller/profile ─────────────────────────────────────────────

    @Test
    void getSellerData_shouldReturnSellerDataDto() throws Exception {
        when(sellerService.getCurrentSellerData()).thenReturn(VALID_SELLER_DATA);

        mockMvc.perform(get("/api/seller/profile")
                        .with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Test Shop"))
                .andExpect(jsonPath("$.billingAddress.street").value("Test Street"))
                .andExpect(jsonPath("$.billingAddress.houseNumber").value("6"))
                .andExpect(jsonPath("$.billingAddress.postalCode").value("58313"))
                .andExpect(jsonPath("$.billingAddress.city").value("Test City"))
                .andExpect(jsonPath("$.billingAddress.country").value("Test Country"))
                .andExpect(jsonPath("$.address.street").value("Test Street"))
                .andExpect(jsonPath("$.address.houseNumber").value("6"))
                .andExpect(jsonPath("$.address.postalCode").value("58313"))
                .andExpect(jsonPath("$.address.city").value("Test City"))
                .andExpect(jsonPath("$.address.country").value("Test Country"))
                .andExpect(jsonPath("$.taxId").value("DE123456789"));

        verify(sellerService).getCurrentSellerData();
    }

    @Test
    void getSellerData_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/seller/profile"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sellerService);
    }

    // ─── PUT /api/seller/profile ─────────────────────────────────────────────

    @Test
    void updateSellerData_shouldReturnUpdatedSellerDataDto() throws Exception {
        SellerDataDto updatedData = new SellerDataDto(
                "Updated Shop",
                new Address("New Street", "10", "12345", "New City", "New Country"),
                new Address("Bill Street", "3", "99999", "Bill City", "Bill Country"),
                "DE987654321");

        when(sellerService.setSellerData(any(SellerDataDto.class))).thenReturn(updatedData);

        String requestJson = """
                {
                    "businessName": "Updated Shop",
                    "address": {
                        "street": "New Street",
                        "houseNumber": "10",
                        "postalCode": "12345",
                        "city": "New City",
                        "country": "New Country"
                    },
                    "billingAddress": {
                        "street": "Bill Street",
                        "houseNumber": "3",
                        "postalCode": "99999",
                        "city": "Bill City",
                        "country": "Bill Country"
                    },
                    "taxId": "DE987654321"
                }
                """;

        mockMvc.perform(put("/api/seller/profile")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Updated Shop"))
                .andExpect(jsonPath("$.address.street").value("New Street"))
                .andExpect(jsonPath("$.billingAddress.street").value("Bill Street"))
                .andExpect(jsonPath("$.taxId").value("DE987654321"));

        verify(sellerService).setSellerData(any(SellerDataDto.class));
    }

    @Test
    void updateSellerData_shouldReturn400_whenBusinessNameIsBlank() throws Exception {
        String invalidJson = """
                {
                    "businessName": "",
                    "address": {
                        "street": "New Street",
                        "houseNumber": "10",
                        "postalCode": "12345",
                        "city": "New City",
                        "country": "New Country"
                    },
                    "billingAddress": {
                        "street": "Bill Street",
                        "houseNumber": "3",
                        "postalCode": "99999",
                        "city": "Bill City",
                        "country": "Bill Country"
                    },
                    "taxId": "DE987654321"
                }
                """;

        mockMvc.perform(put("/api/seller/profile")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(sellerService);
    }

    @Test
    void updateSellerData_shouldReturn400_whenTaxIdIsBlank() throws Exception {
        String invalidJson = """
                {
                    "businessName": "Test Shop",
                    "address": {
                        "street": "New Street",
                        "houseNumber": "10",
                        "postalCode": "12345",
                        "city": "New City",
                        "country": "New Country"
                    },
                    "billingAddress": {
                        "street": "Bill Street",
                        "houseNumber": "3",
                        "postalCode": "99999",
                        "city": "Bill City",
                        "country": "Bill Country"
                    },
                    "taxId": ""
                }
                """;

        mockMvc.perform(put("/api/seller/profile")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(sellerService);
    }

    @Test
    void updateSellerData_shouldReturn401_whenNotAuthenticated() throws Exception {
        String requestJson = """
                {
                    "businessName": "Test Shop",
                    "address": {
                        "street": "Test Street",
                        "houseNumber": "6",
                        "postalCode": "58313",
                        "city": "Test City",
                        "country": "Test Country"
                    },
                    "billingAddress": {
                        "street": "Test Street",
                        "houseNumber": "6",
                        "postalCode": "58313",
                        "city": "Test City",
                        "country": "Test Country"
                    },
                    "taxId": "DE123456789"
                }
                """;

        mockMvc.perform(put("/api/seller/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sellerService);
    }

    @Test
    void updateSellerData_shouldReturn403_withoutCsrfToken() throws Exception {
        String requestJson = """
                {
                    "businessName": "Test Shop",
                    "address": {
                        "street": "Test Street",
                        "houseNumber": "6",
                        "postalCode": "58313",
                        "city": "Test City",
                        "country": "Test Country"
                    },
                    "billingAddress": {
                        "street": "Test Street",
                        "houseNumber": "6",
                        "postalCode": "58313",
                        "city": "Test City",
                        "country": "Test Country"
                    },
                    "taxId": "DE123456789"
                }
                """;

        mockMvc.perform(put("/api/seller/profile")
                        .with(oauth2Login())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

        verifyNoInteractions(sellerService);
    }
}