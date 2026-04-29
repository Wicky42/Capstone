package org.example.backend.seller.controller;

import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.seller.dto.OnboardingStatusResponse;
import org.example.backend.seller.model.OnboardingStep;
import org.example.backend.seller.service.SellerOnboardingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SellerOnboardingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SellerOnboardingService sellerOnboardingService;

    // ─── GET /api/seller/onboarding/status ───────────────────────────────────

    /**
     * Seller ist eingeloggt → Service gibt OnboardingStatusResponse zurück → 200 OK
     */
    @Test
    void getOnboardingStatus_returns200WithResponse_whenSellerIsLoggedIn() throws Exception {
        OnboardingStatusResponse response = new OnboardingStatusResponse(
                false, false, false, false,
                OnboardingStep.START, OnboardingStep.SHOP_CONFIGURATION,
                "Erstelle deinen Shop!"
        );
        when(sellerOnboardingService.getCurrentOnboardingStatus()).thenReturn(response);

        mockMvc.perform(get("/api/seller/onboarding/status")
                        .with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopCreated").value(false))
                .andExpect(jsonPath("$.shopDataCompleted").value(false))
                .andExpect(jsonPath("$.currentStep").value("START"))
                .andExpect(jsonPath("$.nextStep").value("SHOP_CONFIGURATION"))
                .andExpect(jsonPath("$.message").value("Erstelle deinen Shop!"));

        verify(sellerOnboardingService).getCurrentOnboardingStatus();
    }

    /**
     * Eingeloggter Benutzer ist kein Seller (z.B. CUSTOMER) →
     * Service wirft ForbiddenAccessException → 403 Forbidden
     */
    @Test
    void getOnboardingStatus_returns403_whenLoggedInUserIsNotSeller() throws Exception {
        when(sellerOnboardingService.getCurrentOnboardingStatus())
                .thenThrow(new ForbiddenAccessException(
                        "Unerlaubter Zugriff. Nur Seller dürfen diesen Bereich nutzen."));

        mockMvc.perform(get("/api/seller/onboarding/status")
                        .with(oauth2Login()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(
                        "Unerlaubter Zugriff. Nur Seller dürfen diesen Bereich nutzen."));

        verify(sellerOnboardingService).getCurrentOnboardingStatus();
    }

    /**
     * Benutzer ist nicht eingeloggt →
     * Spring Security blockiert per /api/seller/** .authenticated() → 401 Unauthorized.
     * Der Service wird nicht aufgerufen.
     */
    @Test
    void getOnboardingStatus_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/seller/onboarding/status"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sellerOnboardingService);
    }
}

