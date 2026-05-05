package org.example.backend.admin.controller;

import org.example.backend.admin.dto.PendingShopVerificationResponse;
import org.example.backend.admin.service.AdminShopVerificationService;
import org.example.backend.common.exception.*;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.model.ShopStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminShopVerificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminShopVerificationService adminShopVerificationService;

    // ─── VALID DATA ──────────────────────────────────────────────────────────

    private static final String SHOP_ID = "shop-1";

    private ShopResponse activeShopResponse;
    private ShopResponse inactiveShopResponse;
    private ShopResponse rejectedShopResponse;
    private List<PendingShopVerificationResponse> pendingList;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        activeShopResponse = new ShopResponse(
                SHOP_ID, "seller-1", "Test Shop", "Beschreibung",
                null, null, "test-shop", ShopStatus.ACTIVE, now, now
        );

        inactiveShopResponse = new ShopResponse(
                SHOP_ID, "seller-1", "Test Shop", "Beschreibung",
                null, null, "test-shop", ShopStatus.INACTIVE, now, now
        );

        rejectedShopResponse = new ShopResponse(
                SHOP_ID, "seller-1", "Test Shop", "Beschreibung",
                null, null, "test-shop", ShopStatus.REJECTED, now, now
        );

        pendingList = List.of(
                PendingShopVerificationResponse.builder()
                        .shopId(SHOP_ID)
                        .shopName("Test Shop")
                        .shopDescription("Beschreibung")
                        .sellerId("seller-1")
                        .sellerName("Muster GmbH")
                        .sellerEmail("seller@example.com")
                        .shopStatus("DRAFT")
                        .onboardingCompleted(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );
    }

    // ─── GET /api/admin/shops/pending-verification ───────────────────────────

    @Test
    void getPendingShopVerifications_returns200WithList_whenAdminAuthenticated() throws Exception {
        when(adminShopVerificationService.getPendingShopVerifications()).thenReturn(pendingList);

        mockMvc.perform(get("/api/admin/shops/pending-verification")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shopId").value(SHOP_ID))
                .andExpect(jsonPath("$[0].shopName").value("Test Shop"))
                .andExpect(jsonPath("$[0].shopStatus").value("DRAFT"))
                .andExpect(jsonPath("$[0].onboardingCompleted").value(true));

        verify(adminShopVerificationService).getPendingShopVerifications();
    }

    @Test
    void getPendingShopVerifications_returns200WithEmptyList_whenNoPendingShops() throws Exception {
        when(adminShopVerificationService.getPendingShopVerifications()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/shops/pending-verification")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getPendingShopVerifications_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/shops/pending-verification"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminShopVerificationService);
    }

    @Test
    void getPendingShopVerifications_returns403_whenAuthenticatedButNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/shops/pending-verification")
                        .with(oauth2Login()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminShopVerificationService);
    }

    // ─── PUT /api/admin/shops/{shopId}/activate ──────────────────────────────

    @Test
    void activateShop_returns200WithActiveShop_whenAdminAuthenticated() throws Exception {
        when(adminShopVerificationService.activateShop(SHOP_ID)).thenReturn(activeShopResponse);

        mockMvc.perform(put("/api/admin/shops/{shopId}/activate", SHOP_ID)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SHOP_ID))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(adminShopVerificationService).activateShop(SHOP_ID);
    }

    @Test
    void activateShop_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/admin/shops/{shopId}/activate", SHOP_ID)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminShopVerificationService);
    }

    @Test
    void activateShop_returns403_whenAuthenticatedButNotAdmin() throws Exception {
        mockMvc.perform(put("/api/admin/shops/{shopId}/activate", SHOP_ID)
                        .with(oauth2Login())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminShopVerificationService);
    }

    @Test
    void activateShop_returns403_withoutCsrfToken() throws Exception {
        mockMvc.perform(put("/api/admin/shops/{shopId}/activate", SHOP_ID)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminShopVerificationService);
    }

    // ─── PUT /api/admin/shops/{shopId}/deactivate ────────────────────────────

    @Test
    void deactivateShop_returns200WithInactiveShop_whenAdminAuthenticated() throws Exception {
        when(adminShopVerificationService.deactivateShop(SHOP_ID)).thenReturn(inactiveShopResponse);

        mockMvc.perform(put("/api/admin/shops/{shopId}/deactivate", SHOP_ID)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SHOP_ID))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(adminShopVerificationService).deactivateShop(SHOP_ID);
    }

    @Test
    void deactivateShop_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/admin/shops/{shopId}/deactivate", SHOP_ID)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminShopVerificationService);
    }

    @Test
    void deactivateShop_returns403_whenAuthenticatedButNotAdmin() throws Exception {
        mockMvc.perform(put("/api/admin/shops/{shopId}/deactivate", SHOP_ID)
                        .with(oauth2Login())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminShopVerificationService);
    }

    // ─── PUT /api/admin/shops/{shopId}/reject ────────────────────────────────

    @Test
    void rejectShop_returns200WithRejectedShop_whenAdminAuthenticated() throws Exception {
        when(adminShopVerificationService.rejectShop(SHOP_ID)).thenReturn(rejectedShopResponse);

        mockMvc.perform(put("/api/admin/shops/{shopId}/reject", SHOP_ID)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SHOP_ID))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(adminShopVerificationService).rejectShop(SHOP_ID);
    }

    @Test
    void rejectShop_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/admin/shops/{shopId}/reject", SHOP_ID)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminShopVerificationService);
    }

    @Test
    void rejectShop_returns403_whenAuthenticatedButNotAdmin() throws Exception {
        mockMvc.perform(put("/api/admin/shops/{shopId}/reject", SHOP_ID)
                        .with(oauth2Login())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminShopVerificationService);
    }
}

