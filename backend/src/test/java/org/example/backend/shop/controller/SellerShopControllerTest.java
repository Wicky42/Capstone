package org.example.backend.shop.controller;

import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.dto.UpdateShopRequest;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.shop.service.ShopService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SellerShopControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ShopService shopService;

    private ShopResponse buildShopResponse() {
        return new ShopResponse(
                "shop-1",
                "seller-1",
                "Mein Shop",
                "Eine tolle Beschreibung",
                "logo.png",
                "header.png",
                "mein-shop",
                ShopStatus.DRAFT,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // ─── GET /api/seller/shops/my ────────────────────────────────────────────

    @Test
    void getMyShop_returnsShopResponse_whenShopExists() throws Exception {
        ShopResponse response = buildShopResponse();
        when(shopService.getCurrentSellerShop()).thenReturn(response);

        mockMvc.perform(get("/api/seller/shops/my")
                        .with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("shop-1"))
                .andExpect(jsonPath("$.name").value("Mein Shop"))
                .andExpect(jsonPath("$.slug").value("mein-shop"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(shopService).getCurrentSellerShop();
    }

    @Test
    void getMyShop_returns401_whenNotAuthenticated() throws Exception {
        // /api/seller/** ist per SecurityConfig mit .authenticated() geschützt → 401 ohne Login
        mockMvc.perform(get("/api/seller/shops/my"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(shopService);
    }

    // ─── PUT /api/seller/shops/my ────────────────────────────────────────────

    @Test
    void updateMyShop_returnsUpdatedShopResponse_whenRequestIsValid() throws Exception {
        ShopResponse updatedResponse = new ShopResponse(
                "shop-1",
                "seller-1",
                "Aktualisierter Shop",
                "Eine neue tolle Beschreibung",
                "new-logo.png",
                "new-header.png",
                "aktualisierter-shop",
                ShopStatus.DRAFT,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(shopService.updateCurrentSellerShop(any(UpdateShopRequest.class))).thenReturn(updatedResponse);

        String requestJson = """
                {
                    "name": "Aktualisierter Shop",
                    "description": "Eine neue tolle Beschreibung",
                    "logoUrl": "new-logo.png",
                    "headerImageUrl": "new-header.png"
                }
                """;

        mockMvc.perform(put("/api/seller/shops/my")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aktualisierter Shop"))
                .andExpect(jsonPath("$.slug").value("aktualisierter-shop"))
                .andExpect(jsonPath("$.logoUrl").value("new-logo.png"))
                .andExpect(jsonPath("$.headerImageUrl").value("new-header.png"));

        verify(shopService).updateCurrentSellerShop(any(UpdateShopRequest.class));
    }

    @Test
    void updateMyShop_returns400_whenNameIsBlank() throws Exception {
        String invalidJson = """
                {
                    "name": "",
                    "description": "Eine ausreichend lange Beschreibung",
                    "logoUrl": null,
                    "headerImageUrl": null
                }
                """;

        mockMvc.perform(put("/api/seller/shops/my")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(shopService);
    }

    @Test
    void updateMyShop_returns400_whenDescriptionIsTooShort() throws Exception {
        String invalidJson = """
                {
                    "name": "Gültiger Name",
                    "description": "Zu kurz",
                    "logoUrl": null,
                    "headerImageUrl": null
                }
                """;

        mockMvc.perform(put("/api/seller/shops/my")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(shopService);
    }

    @Test
    void updateMyShop_returns403_withoutCsrfToken() throws Exception {
        String requestJson = """
                {
                    "name": "Mein Shop",
                    "description": "Eine tolle ausreichende Beschreibung",
                    "logoUrl": null,
                    "headerImageUrl": null
                }
                """;

        mockMvc.perform(put("/api/seller/shops/my")
                        .with(oauth2Login())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

        verifyNoInteractions(shopService);
    }
}










