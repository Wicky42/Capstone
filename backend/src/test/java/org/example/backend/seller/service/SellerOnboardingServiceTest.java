package org.example.backend.seller.service;

import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.seller.dto.OnboardingStatusResponse;
import org.example.backend.seller.model.OnboardingStep;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Customer;
import org.example.backend.user.model.Seller;
import org.example.backend.user.model.User;
import org.example.backend.user.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerOnboardingServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    ShopRepository shopRepository;

    @InjectMocks
    SellerOnboardingService sellerOnboardingService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ── Hilfsmethoden ──────────────────────────────────────────────────────────

    private Seller buildSeller(String id, String shopId) {
        return Seller.builder()
                .id(id)
                .role(User.Role.SELLER)
                .name("Test Seller")
                .email("seller@example.com")
                .oauthProvider(User.OAuthProvider.GITHUB)
                .oauthProviderUserId("gh-seller-1")
                .shopId(shopId)
                .build();
    }

    private void authenticateAs(User user) {
        var auth = new TestingAuthenticationToken(user, null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ── getCurrentOnBoardingStatus ─────────────────────────────────────────────

    @Test
    void getCurrentOnBoardingStatus_returnsStartStep_whenSellerHasNoShop() {
        Seller seller = buildSeller("seller-1", null);
        authenticateAs(seller);
        when(userRepository.findById("seller-1")).thenReturn(Optional.of(seller));

        OnboardingStatusResponse response = sellerOnboardingService.getCurrentOnBoardingStatus();

        assertThat(response.shopCreated()).isFalse();
        assertThat(response.currentStep()).isEqualTo(OnboardingStep.START);
        assertThat(response.nextStep()).isEqualTo(OnboardingStep.PRODUCT_CREATION);
        assertThat(response.message()).isEqualTo("Erstelle deinen Shop!");
    }

    @Test
    void getCurrentOnBoardingStatus_returnsStartStep_whenShopIdSetButShopNotInDb() {
        Seller seller = buildSeller("seller-2", "shop-999");
        authenticateAs(seller);
        when(userRepository.findById("seller-2")).thenReturn(Optional.of(seller));
        when(shopRepository.existsById("shop-999")).thenReturn(false);

        OnboardingStatusResponse response = sellerOnboardingService.getCurrentOnBoardingStatus();

        assertThat(response.shopCreated()).isFalse();
        assertThat(response.currentStep()).isEqualTo(OnboardingStep.START);
    }

    @Test
    void getCurrentOnBoardingStatus_returnsShopCreationStep_whenShopExists() {
        Seller seller = buildSeller("seller-3", "shop-1");
        authenticateAs(seller);
        when(userRepository.findById("seller-3")).thenReturn(Optional.of(seller));
        when(shopRepository.existsById("shop-1")).thenReturn(true);

        OnboardingStatusResponse response = sellerOnboardingService.getCurrentOnBoardingStatus();

        assertThat(response.shopCreated()).isTrue();
        assertThat(response.currentStep()).isEqualTo(OnboardingStep.SHOP_CREATION);
        assertThat(response.nextStep()).isEqualTo(OnboardingStep.SHOP_CONFIGURATION);
        assertThat(response.message()).isEqualTo("Vervollständige deine Shop-Daten.");
    }

    // ── Fehlerszenarien ────────────────────────────────────────────────────────

    @Test
    void getCurrentOnBoardingStatus_throwsForbiddenAccess_whenNotAuthenticated() {
        // SecurityContextHolder bleibt leer – kein Principal gesetzt
        assertThatThrownBy(() -> sellerOnboardingService.getCurrentOnBoardingStatus())
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("Kein eingeloggter Benutzer gefunden.");
    }

    @Test
    void getCurrentOnBoardingStatus_throwsForbiddenAccess_whenAuthenticatedUserIsNotSeller() {
        Customer customer = Customer.builder()
                .id("customer-1")
                .role(User.Role.CUSTOMER)
                .name("Max Kunde")
                .email("max@example.com")
                .oauthProvider(User.OAuthProvider.GITHUB)
                .oauthProviderUserId("gh-cust-1")
                .build();

        authenticateAs(customer);
        when(userRepository.findById("customer-1")).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> sellerOnboardingService.getCurrentOnBoardingStatus())
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("Nur Seller");
    }

    @Test
    void getCurrentOnBoardingStatus_throwsRuntimeException_whenUserNotFoundInDb() {
        Seller seller = buildSeller("seller-unknown", null);
        authenticateAs(seller);
        when(userRepository.findById("seller-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerOnboardingService.getCurrentOnBoardingStatus())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Benutzer nicht gefunden");
    }
}
