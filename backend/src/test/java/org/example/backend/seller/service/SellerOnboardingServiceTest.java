package org.example.backend.seller.service;

import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.product.repository.ProductRepository;
import org.example.backend.seller.dto.OnboardingStatusResponse;
import org.example.backend.seller.model.OnboardingStep;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.model.User;
import org.example.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerOnboardingServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SellerOnboardingService sellerOnboardingService;

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

    private Seller buildSellerWithLegalData(String id, String shopId) {
        return Seller.builder()
                .id(id)
                .role(User.Role.SELLER)
                .name("Test Seller")
                .email("seller@example.com")
                .oauthProvider(User.OAuthProvider.GITHUB)
                .oauthProviderUserId("gh-seller-1")
                .shopId(shopId)
                .businessName("Test GmbH")
                .taxId("DE123456789")
                .build();
    }

    @Test
    void getCurrentOnboardingStatus_returnsStartStep_whenSellerHasNoShop() {
        Seller seller = buildSeller("seller-1", null);
        when(userService.getCurrentSeller()).thenReturn(seller);

        OnboardingStatusResponse response = sellerOnboardingService.getCurrentOnboardingStatus();

        assertThat(response.shopCreated()).isFalse();
        assertThat(response.currentStep()).isEqualTo(OnboardingStep.START);
        assertThat(response.nextStep()).isEqualTo(OnboardingStep.SHOP_CONFIGURATION);
        assertThat(response.message()).isEqualTo("Erstelle deinen Shop!");

        verify(userService).getCurrentSeller();
        verifyNoInteractions(shopRepository);
    }

    @Test
    void getCurrentOnboardingStatus_returnsStartStep_whenShopIdSetButShopNotInDb() {
        Seller seller = buildSeller("seller-2", "shop-999");
        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.existsById("shop-999")).thenReturn(false);

        OnboardingStatusResponse response = sellerOnboardingService.getCurrentOnboardingStatus();

        assertThat(response.shopCreated()).isFalse();
        assertThat(response.currentStep()).isEqualTo(OnboardingStep.START);
        assertThat(response.nextStep()).isEqualTo(OnboardingStep.SHOP_CONFIGURATION);
        assertThat(response.message()).isEqualTo("Erstelle deinen Shop!");

        verify(userService).getCurrentSeller();
        verify(shopRepository).existsById("shop-999");
    }

    @Test
    void getCurrentOnboardingStatus_returnsShopConfigurationStep_whenShopExistsButSellerDataMissing() {
        // Seller hat Shop, aber keine rechtlichen Angaben (businessName/taxId fehlen)
        Seller seller = buildSeller("seller-3", "shop-1");
        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.existsById("shop-1")).thenReturn(true);

        OnboardingStatusResponse response = sellerOnboardingService.getCurrentOnboardingStatus();

        assertThat(response.shopCreated()).isTrue();
        assertThat(response.shopDataCompleted()).isFalse();
        assertThat(response.currentStep()).isEqualTo(OnboardingStep.SHOP_CONFIGURATION);
        assertThat(response.nextStep()).isEqualTo(OnboardingStep.PRODUCT_CREATION);
        assertThat(response.message()).isEqualTo("Vervollständige deine rechtlichen Angaben.");

        verify(userService).getCurrentSeller();
        verify(shopRepository).existsById("shop-1");
        verifyNoMoreInteractions(shopRepository);
    }

    @Test
    void getCurrentOnboardingStatus_returnsProductCreationStep_whenSellerDataCompleteButNoProduct() {
        Seller seller = buildSellerWithLegalData("seller-4", "shop-1");
        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.existsById("shop-1")).thenReturn(true);
        when(shopRepository.findBySellerId("seller-4")).thenReturn(
                java.util.Optional.of(org.example.backend.shop.model.Shop.builder()
                        .id("shop-1").sellerId("seller-4").build())
        );
        when(productRepository.existsByShopId("shop-1")).thenReturn(false);

        OnboardingStatusResponse response = sellerOnboardingService.getCurrentOnboardingStatus();

        assertThat(response.shopCreated()).isTrue();
        assertThat(response.shopDataCompleted()).isTrue();
        assertThat(response.firstProductCreated()).isFalse();
        assertThat(response.currentStep()).isEqualTo(OnboardingStep.PRODUCT_CREATION);
        assertThat(response.nextStep()).isEqualTo(OnboardingStep.COMPLETED);
        assertThat(response.message()).isEqualTo("Füge dein erstes Produkt ein.");
    }

    @Test
    void getCurrentOnboardingStatus_returnsCompletedStep_whenAllStepsDone() {
        Seller seller = buildSellerWithLegalData("seller-5", "shop-1");
        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.existsById("shop-1")).thenReturn(true);
        when(shopRepository.findBySellerId("seller-5")).thenReturn(
                java.util.Optional.of(org.example.backend.shop.model.Shop.builder()
                        .id("shop-1").sellerId("seller-5").build())
        );
        when(productRepository.existsByShopId("shop-1")).thenReturn(true);

        OnboardingStatusResponse response = sellerOnboardingService.getCurrentOnboardingStatus();

        assertThat(response.shopCreated()).isTrue();
        assertThat(response.shopDataCompleted()).isTrue();
        assertThat(response.firstProductCreated()).isTrue();
        assertThat(response.onBoardingCompleted()).isTrue();
        assertThat(response.currentStep()).isEqualTo(OnboardingStep.COMPLETED);
    }

    @Test
    void getCurrentOnBoardingStatus_propagatesForbiddenAccess_whenCurrentUserIsNotSeller() {
        when(userService.getCurrentSeller())
                .thenThrow(new ForbiddenAccessException("Unerlaubter Zugriff. Nur Seller dürfen diesen Bereich nutzen."));

        assertThatThrownBy(() -> sellerOnboardingService.getCurrentOnboardingStatus())
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("Nur Seller");

        verify(userService).getCurrentSeller();
        verifyNoInteractions(shopRepository);
    }

    @Test
    void getCurrentOnboardingStatus_propagatesForbiddenAccess_whenNotAuthenticated() {
        when(userService.getCurrentSeller())
                .thenThrow(new ForbiddenAccessException("Kein eingeloggter Benutzer gefunden."));

        assertThatThrownBy(() -> sellerOnboardingService.getCurrentOnboardingStatus())
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("Kein eingeloggter Benutzer gefunden");

        verify(userService).getCurrentSeller();
        verifyNoInteractions(shopRepository);
    }
}