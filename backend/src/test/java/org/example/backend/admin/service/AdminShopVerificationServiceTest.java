package org.example.backend.admin.service;

import org.example.backend.admin.dto.PendingShopVerificationResponse;
import org.example.backend.common.exception.*;
import org.example.backend.product.repository.ProductRepository;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.model.Shop;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.model.User;
import org.example.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminShopVerificationServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AdminShopVerificationService adminShopVerificationService;

    // ─── VALID DATA ──────────────────────────────────────────────────────────

    private static final String SHOP_ID   = "shop-1";
    private static final String SELLER_ID = "seller-1";

    private Shop draftShop;
    private Shop activeShop;
    private Shop inactiveShop;
    private Shop rejectedShop;
    private Seller completedSeller;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        draftShop = Shop.builder()
                .id(SHOP_ID)
                .sellerId(SELLER_ID)
                .name("Test Shop")
                .description("Eine tolle Beschreibung")
                .slug("test-shop")
                .status(ShopStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();

        activeShop = Shop.builder()
                .id(SHOP_ID)
                .sellerId(SELLER_ID)
                .name("Test Shop")
                .description("Eine tolle Beschreibung")
                .slug("test-shop")
                .status(ShopStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        inactiveShop = Shop.builder()
                .id(SHOP_ID)
                .sellerId(SELLER_ID)
                .name("Test Shop")
                .description("Eine tolle Beschreibung")
                .slug("test-shop")
                .status(ShopStatus.INACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        rejectedShop = Shop.builder()
                .id(SHOP_ID)
                .sellerId(SELLER_ID)
                .name("Test Shop")
                .description("Eine tolle Beschreibung")
                .slug("test-shop")
                .status(ShopStatus.REJECTED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        completedSeller = Seller.builder()
                .id(SELLER_ID)
                .role(User.Role.SELLER)
                .name("Max Muster")
                .email("seller@example.com")
                .businessName("Muster GmbH")
                .onboardingCompleted(true)
                .build();
    }

    // ─── getPendingShopVerifications ─────────────────────────────────────────

    @Test
    void getPendingShopVerifications_returnsListWithResponse_whenAllConditionsMet() {
        when(shopRepository.findByStatus(ShopStatus.DRAFT)).thenReturn(List.of(draftShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(completedSeller));
        when(productRepository.existsByShopId(SHOP_ID)).thenReturn(true);

        List<PendingShopVerificationResponse> result = adminShopVerificationService.getPendingShopVerifications();

        assertThat(result).hasSize(1);
        PendingShopVerificationResponse response = result.getFirst();
        assertThat(response.getShopId()).isEqualTo(SHOP_ID);
        assertThat(response.getSellerId()).isEqualTo(SELLER_ID);
        assertThat(response.getShopName()).isEqualTo("Test Shop");
        assertThat(response.getSellerName()).isEqualTo("Muster GmbH");
        assertThat(response.getSellerEmail()).isEqualTo("seller@example.com");
        assertThat(response.getShopStatus()).isEqualTo("DRAFT");
        assertThat(response.isOnboardingCompleted()).isTrue();
    }

    @Test
    void getPendingShopVerifications_returnsEmptyList_whenNoDraftShopsExist() {
        when(shopRepository.findByStatus(ShopStatus.DRAFT)).thenReturn(List.of());

        List<PendingShopVerificationResponse> result = adminShopVerificationService.getPendingShopVerifications();

        assertThat(result).isEmpty();
        verifyNoInteractions(userRepository, productRepository);
    }

    @Test
    void getPendingShopVerifications_excludesShop_whenOnboardingNotCompleted() {
        Seller incompleteSeller = Seller.builder()
                .id(SELLER_ID)
                .role(User.Role.SELLER)
                .name("Lena Muster")
                .email("lena@example.com")
                .onboardingCompleted(false)
                .build();

        when(shopRepository.findByStatus(ShopStatus.DRAFT)).thenReturn(List.of(draftShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(incompleteSeller));

        List<PendingShopVerificationResponse> result = adminShopVerificationService.getPendingShopVerifications();

        assertThat(result).isEmpty();
    }

    @Test
    void getPendingShopVerifications_excludesShop_whenSellerNotFound() {
        when(shopRepository.findByStatus(ShopStatus.DRAFT)).thenReturn(List.of(draftShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.empty());

        List<PendingShopVerificationResponse> result = adminShopVerificationService.getPendingShopVerifications();

        assertThat(result).isEmpty();
    }

    @Test
    void getPendingShopVerifications_excludesShop_whenUserIsNotASeller() {
        User nonSeller = mock(User.class);

        when(shopRepository.findByStatus(ShopStatus.DRAFT)).thenReturn(List.of(draftShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of((User) nonSeller));

        // nonSeller ist kein Seller → instanceof-Check schlägt fehl
        List<PendingShopVerificationResponse> result = adminShopVerificationService.getPendingShopVerifications();

        assertThat(result).isEmpty();
    }

    @Test
    void getPendingShopVerifications_excludesShop_whenShopHasNoProducts() {
        when(shopRepository.findByStatus(ShopStatus.DRAFT)).thenReturn(List.of(draftShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(completedSeller));
        when(productRepository.existsByShopId(SHOP_ID)).thenReturn(false);

        List<PendingShopVerificationResponse> result = adminShopVerificationService.getPendingShopVerifications();

        assertThat(result).isEmpty();
    }

    // ─── activateShop ────────────────────────────────────────────────────────

    @Test
    void activateShop_returnsActiveShopResponse_whenAllConditionsMet() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(draftShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(completedSeller));
        when(productRepository.existsByShopId(SHOP_ID)).thenReturn(true);
        when(shopRepository.save(any(Shop.class))).thenAnswer(inv -> inv.getArgument(0));

        ShopResponse response = adminShopVerificationService.activateShop(SHOP_ID);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ShopStatus.ACTIVE);
        verify(shopRepository).save(draftShop);
    }

    @Test
    void activateShop_throwsShopNotFoundException_whenShopDoesNotExist() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminShopVerificationService.activateShop(SHOP_ID))
                .isInstanceOf(ShopNotFoundException.class);

        verify(shopRepository, never()).save(any());
    }

    @Test
    void activateShop_throwsShopAlreadyActiveException_whenShopIsAlreadyActive() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(activeShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(completedSeller));

        assertThatThrownBy(() -> adminShopVerificationService.activateShop(SHOP_ID))
                .isInstanceOf(ShopAlreadyActiveException.class);

        verify(shopRepository, never()).save(any());
    }

    @Test
    void activateShop_throwsShopRejectedException_whenShopIsRejected() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(rejectedShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(completedSeller));

        assertThatThrownBy(() -> adminShopVerificationService.activateShop(SHOP_ID))
                .isInstanceOf(ShopRejectedException.class);

        verify(shopRepository, never()).save(any());
    }

    @Test
    void activateShop_throwsSellerOnboardingIncompleteException_whenOnboardingNotDone() {
        Seller incompleteSeller = Seller.builder()
                .id(SELLER_ID)
                .role(User.Role.SELLER)
                .name("Lena Muster")
                .email("lena@example.com")
                .onboardingCompleted(false)
                .build();

        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(draftShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(incompleteSeller));

        assertThatThrownBy(() -> adminShopVerificationService.activateShop(SHOP_ID))
                .isInstanceOf(SellerOnboardingIncompleteException.class);

        verify(shopRepository, never()).save(any());
    }

    @Test
    void activateShop_throwsShopHasNoProductsException_whenNoProductsExist() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(draftShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(completedSeller));
        when(productRepository.existsByShopId(SHOP_ID)).thenReturn(false);

        assertThatThrownBy(() -> adminShopVerificationService.activateShop(SHOP_ID))
                .isInstanceOf(ShopHasNoProductsException.class);

        verify(shopRepository, never()).save(any());
    }

    // ─── deactivateShop ──────────────────────────────────────────────────────

    @Test
    void deactivateShop_returnsInactiveShopResponse_whenAllConditionsMet() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(activeShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(completedSeller));
        when(shopRepository.save(any(Shop.class))).thenAnswer(inv -> inv.getArgument(0));

        ShopResponse response = adminShopVerificationService.deactivateShop(SHOP_ID);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ShopStatus.INACTIVE);
        verify(shopRepository).save(activeShop);
    }

    @Test
    void deactivateShop_throwsShopNotFoundException_whenShopDoesNotExist() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminShopVerificationService.deactivateShop(SHOP_ID))
                .isInstanceOf(ShopNotFoundException.class);

        verify(shopRepository, never()).save(any());
    }

    @Test
    void deactivateShop_throwsShopAlreadyInactiveException_whenShopIsAlreadyInactive() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(inactiveShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(completedSeller));

        assertThatThrownBy(() -> adminShopVerificationService.deactivateShop(SHOP_ID))
                .isInstanceOf(ShopAlreadyInactiveException.class);

        verify(shopRepository, never()).save(any());
    }

    @Test
    void deactivateShop_throwsShopRejectedException_whenShopIsRejected() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(rejectedShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(completedSeller));

        assertThatThrownBy(() -> adminShopVerificationService.deactivateShop(SHOP_ID))
                .isInstanceOf(ShopRejectedException.class);

        verify(shopRepository, never()).save(any());
    }

    @Test
    void deactivateShop_throwsSellerOnboardingIncompleteException_whenOnboardingNotDone() {
        Seller incompleteSeller = Seller.builder()
                .id(SELLER_ID)
                .role(User.Role.SELLER)
                .name("Lena Muster")
                .email("lena@example.com")
                .onboardingCompleted(false)
                .build();

        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(activeShop));
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.of(incompleteSeller));

        assertThatThrownBy(() -> adminShopVerificationService.deactivateShop(SHOP_ID))
                .isInstanceOf(SellerOnboardingIncompleteException.class);

        verify(shopRepository, never()).save(any());
    }

    // ─── rejectShop ──────────────────────────────────────────────────────────

    @Test
    void rejectShop_returnsRejectedShopResponse_whenShopIsDraft() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(draftShop));
        when(shopRepository.save(any(Shop.class))).thenAnswer(inv -> inv.getArgument(0));

        ShopResponse response = adminShopVerificationService.rejectShop(SHOP_ID);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ShopStatus.REJECTED);
        verify(shopRepository).save(draftShop);
    }

    @Test
    void rejectShop_throwsShopNotFoundException_whenShopDoesNotExist() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminShopVerificationService.rejectShop(SHOP_ID))
                .isInstanceOf(ShopNotFoundException.class);

        verify(shopRepository, never()).save(any());
    }

    @Test
    void rejectShop_throwsShopRejectedException_whenShopIsAlreadyRejected() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(rejectedShop));

        assertThatThrownBy(() -> adminShopVerificationService.rejectShop(SHOP_ID))
                .isInstanceOf(ShopRejectedException.class);

        verify(shopRepository, never()).save(any());
    }

    @Test
    void rejectShop_throwsInvalidShopStateException_whenShopIsActive() {
        when(shopRepository.findById(SHOP_ID)).thenReturn(Optional.of(activeShop));

        assertThatThrownBy(() -> adminShopVerificationService.rejectShop(SHOP_ID))
                .isInstanceOf(InvalidShopStateException.class);

        verify(shopRepository, never()).save(any());
    }
}


