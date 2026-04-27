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
                OnboardingStep.START, OnboardingStep.SHOP_CREATION,
                "Erstelle deinen Shop!"
        );
        when(sellerOnboardingService.getCurrentOnBoardingStatus()).thenReturn(response);

        mockMvc.perform(get("/api/seller/onboarding/status")
                        .with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopCreated").value(false))
                .andExpect(jsonPath("$.shopDataCompleted").value(false))
                .andExpect(jsonPath("$.currentStep").value("START"))
                .andExpect(jsonPath("$.nextStep").value("SHOP_CREATION"))
                .andExpect(jsonPath("$.message").value("Erstelle deinen Shop!"));

        verify(sellerOnboardingService).getCurrentOnBoardingStatus();
    }

    /**
     * Eingeloggter Benutzer ist kein Seller (z.B. CUSTOMER) →
     * Service wirft ForbiddenAccessException → 403 Forbidden
     */
    @Test
    void getOnboardingStatus_returns403_whenLoggedInUserIsNotSeller() throws Exception {
        when(sellerOnboardingService.getCurrentOnBoardingStatus())
                .thenThrow(new ForbiddenAccessException(
                        "Unerlaubter Zugriff. Nur Seller dürfen diesen Bereich nutzen."));

        mockMvc.perform(get("/api/seller/onboarding/status")
                        .with(oauth2Login()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(
                        "Unerlaubter Zugriff. Nur Seller dürfen diesen Bereich nutzen."));

        verify(sellerOnboardingService).getCurrentOnBoardingStatus();
    }

    /**
     * Benutzer ist nicht eingeloggt →
     * Service wirft ForbiddenAccessException → 403 Forbidden
     */
    @Test
    void getOnboardingStatus_returns403_whenNotAuthenticated() throws Exception {
        when(sellerOnboardingService.getCurrentOnBoardingStatus())
                .thenThrow(new ForbiddenAccessException("Kein eingeloggter Benutzer gefunden."));

        mockMvc.perform(get("/api/seller/onboarding/status"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Kein eingeloggter Benutzer gefunden."));

        verify(sellerOnboardingService).getCurrentOnBoardingStatus();
    }
}

