package org.example.backend.shop.service;

import org.example.backend.common.exception.ShopNotFoundException;
import org.example.backend.seller.service.SellerService;
import org.example.backend.shop.dto.CreateShopRequest;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.dto.UpdateShopRequest;
import org.example.backend.shop.model.Shop;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.model.User;
import org.example.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private SellerService sellerService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ShopService shopService;

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

    private Shop buildShop(String id, String sellerId) {
        return Shop.builder()
                .id(id)
                .sellerId(sellerId)
                .name("Mein Shop")
                .description("Eine tolle Beschreibung")
                .slug("mein-shop")
                .status(ShopStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Shop buildActiveShop(String id, String sellerId) {
        return Shop.builder()
                .id(id)
                .sellerId(sellerId)
                .name("Aktiver Shop")
                .description("Beschreibung")
                .slug("aktiver-shop")
                .status(ShopStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    // ─── Erfolgreiche Erstellung ────────────────────────────────────────────

    @Test
    void createShopForSeller_returnsShopResponse_whenEverythingIsValid() {
        Seller seller = buildSeller("seller-1", null);
        CreateShopRequest request = new CreateShopRequest("Mein Shop", "Eine tolle Beschreibung");
        Shop savedShop = buildShop("shop-1", "seller-1");

        when(shopRepository.existsByName("Mein Shop")).thenReturn(false);
        when(shopRepository.findBySellerId("seller-1")).thenReturn(Optional.empty());
        when(shopRepository.save(any(Shop.class))).thenReturn(savedShop);
        when(sellerService.linkShopToSeller(any(Seller.class), any(Shop.class))).thenReturn(true);

        ShopResponse response = shopService.createShopForSeller(seller, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("shop-1");
        assertThat(response.sellerId()).isEqualTo("seller-1");
        assertThat(response.name()).isEqualTo("Mein Shop");
        assertThat(response.status()).isEqualTo(ShopStatus.DRAFT);

        verify(shopRepository).existsByName("Mein Shop");
        verify(shopRepository).findBySellerId("seller-1");
        verify(shopRepository).save(any(Shop.class));
        verify(sellerService).linkShopToSeller(any(Seller.class), any(Shop.class));
    }

    @Test
    void createShopForSeller_savesShopWithCorrectSlug() {
        Seller seller = buildSeller("seller-1", null);
        CreateShopRequest request = new CreateShopRequest("Mein Schöner Shop", "Eine tolle Beschreibung");
        Shop savedShop = buildShop("shop-1", "seller-1");

        when(shopRepository.existsByName(any())).thenReturn(false);
        when(shopRepository.findBySellerId("seller-1")).thenReturn(Optional.empty());
        when(shopRepository.save(any(Shop.class))).thenReturn(savedShop);
        when(sellerService.linkShopToSeller(any(), any())).thenReturn(true);

        shopService.createShopForSeller(seller, request);

        ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository).save(captor.capture());
        Shop capturedShop = captor.getValue();

        assertThat(capturedShop.getSlug()).isEqualTo("mein-schoner-shop");
    }

    @Test
    void createShopForSeller_savesShopWithStatusDraft() {
        Seller seller = buildSeller("seller-1", null);
        CreateShopRequest request = new CreateShopRequest("Mein Shop", "Eine tolle Beschreibung");
        Shop savedShop = buildShop("shop-1", "seller-1");

        when(shopRepository.existsByName(any())).thenReturn(false);
        when(shopRepository.findBySellerId("seller-1")).thenReturn(Optional.empty());
        when(shopRepository.save(any(Shop.class))).thenReturn(savedShop);
        when(sellerService.linkShopToSeller(any(), any())).thenReturn(true);

        shopService.createShopForSeller(seller, request);

        ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(ShopStatus.DRAFT);
    }

    // ─── Shop-Name bereits vergeben ─────────────────────────────────────────

    @Test
    void createShopForSeller_throwsIllegalState_whenShopNameAlreadyExists() {
        Seller seller = buildSeller("seller-1", null);
        CreateShopRequest request = new CreateShopRequest("Mein Shop", "Eine tolle Beschreibung");

        when(shopRepository.existsByName("Mein Shop")).thenReturn(true);

        assertThatThrownBy(() -> shopService.createShopForSeller(seller, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ein Shop mit diesem Namen existiert bereits");

        verify(shopRepository).existsByName("Mein Shop");
        verify(shopRepository, never()).save(any());
        verifyNoInteractions(sellerService);
    }

    // ─── Seller hat bereits einen Shop (shopId gesetzt) ─────────────────────

    @Test
    void createShopForSeller_throwsIllegalState_whenSellerShopIdIsAlreadySet() {
        Seller seller = buildSeller("seller-2", "existing-shop-id");
        CreateShopRequest request = new CreateShopRequest("Neuer Shop", "Eine tolle Beschreibung");

        when(shopRepository.existsByName("Neuer Shop")).thenReturn(false);

        assertThatThrownBy(() -> shopService.createShopForSeller(seller, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Händler hat bereits einen Shop");

        verify(shopRepository, never()).save(any());
        verifyNoInteractions(sellerService);
    }

    // ─── Seller hat bereits einen Shop (Shop in DB gefunden) ────────────────

    @Test
    void createShopForSeller_throwsIllegalState_whenShopForSellerExistsInDb() {
        Seller seller = buildSeller("seller-3", null);
        CreateShopRequest request = new CreateShopRequest("Neuer Shop", "Eine tolle Beschreibung");
        Shop existingShop = buildShop("old-shop", "seller-3");

        when(shopRepository.existsByName("Neuer Shop")).thenReturn(false);
        when(shopRepository.findBySellerId("seller-3")).thenReturn(Optional.of(existingShop));

        assertThatThrownBy(() -> shopService.createShopForSeller(seller, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Händler hat bereits einen Shop");

        verify(shopRepository, never()).save(any());
        verifyNoInteractions(sellerService);
    }

    // ─── Verknüpfung schlägt fehl ────────────────────────────────────────────

    @Test
    void createShopForSeller_throwsIllegalState_whenLinkShopToSellerFails() {
        Seller seller = buildSeller("seller-4", null);
        CreateShopRequest request = new CreateShopRequest("Mein Shop", "Eine tolle Beschreibung");
        Shop savedShop = buildShop("shop-4", "seller-4");

        when(shopRepository.existsByName("Mein Shop")).thenReturn(false);
        when(shopRepository.findBySellerId("seller-4")).thenReturn(Optional.empty());
        when(shopRepository.save(any(Shop.class))).thenReturn(savedShop);
        when(sellerService.linkShopToSeller(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> shopService.createShopForSeller(seller, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fehler beim Verknüpfen von Shop und Händler");

        verify(shopRepository).save(any());
        verify(sellerService).linkShopToSeller(any(), any());
    }

    // ─── Slug-Erzeugung ──────────────────────────────────────────────────────

    @Test
    void createShopForSeller_createsSlugWithLowercaseAndHyphens() {
        Seller seller = buildSeller("seller-5", null);
        CreateShopRequest request = new CreateShopRequest("My Cool Shop 2024", "Eine tolle Beschreibung");
        Shop savedShop = buildShop("shop-5", "seller-5");

        when(shopRepository.existsByName(any())).thenReturn(false);
        when(shopRepository.findBySellerId("seller-5")).thenReturn(Optional.empty());
        when(shopRepository.save(any(Shop.class))).thenReturn(savedShop);
        when(sellerService.linkShopToSeller(any(), any())).thenReturn(true);

        shopService.createShopForSeller(seller, request);

        ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository).save(captor.capture());

        assertThat(captor.getValue().getSlug()).isEqualTo("my-cool-shop-2024");
    }

    @Test
    void createShopForSeller_createsSlugWithoutSpecialCharacters() {
        Seller seller = buildSeller("seller-6", null);
        CreateShopRequest request = new CreateShopRequest("Shop & More!", "Eine tolle Beschreibung");
        Shop savedShop = buildShop("shop-6", "seller-6");

        when(shopRepository.existsByName(any())).thenReturn(false);
        when(shopRepository.findBySellerId("seller-6")).thenReturn(Optional.empty());
        when(shopRepository.save(any(Shop.class))).thenReturn(savedShop);
        when(sellerService.linkShopToSeller(any(), any())).thenReturn(true);

        shopService.createShopForSeller(seller, request);

        ArgumentCaptor<Shop> captor = ArgumentCaptor.forClass(Shop.class);
        verify(shopRepository).save(captor.capture());

        assertThat(captor.getValue().getSlug()).isEqualTo("shop-more");
    }

    // ─── getCurrentSellerShop ────────────────────────────────────────────────

    @Test
    void getCurrentSellerShop_returnsShopResponse_whenShopExists() {
        Seller seller = buildSeller("seller-1", "shop-1");
        Shop shop = buildShop("shop-1", "seller-1");

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.findBySellerId("seller-1")).thenReturn(Optional.of(shop));

        ShopResponse response = shopService.getCurrentSellerShop();

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("shop-1");
        assertThat(response.sellerId()).isEqualTo("seller-1");
        assertThat(response.name()).isEqualTo("Mein Shop");

        verify(userService).getCurrentSeller();
        verify(shopRepository).findBySellerId("seller-1");
    }

    @Test
    void getCurrentSellerShop_throwsIllegalState_whenShopNotFound() {
        Seller seller = buildSeller("seller-2", null);

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.findBySellerId("seller-2")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.getCurrentSellerShop())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Der Shop des Händlers wurde nicht gefunden");

        verify(userService).getCurrentSeller();
        verify(shopRepository).findBySellerId("seller-2");
    }

    // ─── updateCurrentSellerShop ─────────────────────────────────────────────

    @Test
    void updateCurrentSellerShop_returnsUpdatedResponse_whenNameUnchanged() {
        Seller seller = buildSeller("seller-1", "shop-1");
        Shop existingShop = buildShop("shop-1", "seller-1");
        UpdateShopRequest request = new UpdateShopRequest("Mein Shop", "Neue Beschreibung hier", "logo.png", "header.png");

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.findBySellerId("seller-1")).thenReturn(Optional.of(existingShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(existingShop);

        ShopResponse response = shopService.updateCurrentSellerShop(request);

        assertThat(response).isNotNull();
        verify(shopRepository, never()).existsByName(any());
        verify(shopRepository).save(existingShop);
    }

    @Test
    void updateCurrentSellerShop_updatesSlug_whenNameChanged() {
        Seller seller = buildSeller("seller-1", "shop-1");
        Shop existingShop = buildShop("shop-1", "seller-1");
        UpdateShopRequest request = new UpdateShopRequest("Neuer Shop Name", "Neue Beschreibung hier", null, null);

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.findBySellerId("seller-1")).thenReturn(Optional.of(existingShop));
        when(shopRepository.existsByName("Neuer Shop Name")).thenReturn(false);
        when(shopRepository.save(any(Shop.class))).thenReturn(existingShop);

        shopService.updateCurrentSellerShop(request);

        assertThat(existingShop.getSlug()).isEqualTo("neuer-shop-name");
        verify(shopRepository).existsByName("Neuer Shop Name");
        verify(shopRepository).save(existingShop);
    }

    @Test
    void updateCurrentSellerShop_throwsIllegalState_whenNewNameAlreadyTaken() {
        Seller seller = buildSeller("seller-1", "shop-1");
        Shop existingShop = buildShop("shop-1", "seller-1");
        UpdateShopRequest request = new UpdateShopRequest("Bereits Vergeben", "Neue Beschreibung hier", null, null);

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.findBySellerId("seller-1")).thenReturn(Optional.of(existingShop));
        when(shopRepository.existsByName("Bereits Vergeben")).thenReturn(true);

        assertThatThrownBy(() -> shopService.updateCurrentSellerShop(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ein Shop mit diesem Namen existiert bereits");

        verify(shopRepository, never()).save(any());
    }

    @Test
    void updateCurrentSellerShop_updatesFields_correctly() {
        Seller seller = buildSeller("seller-1", "shop-1");
        Shop existingShop = buildShop("shop-1", "seller-1");
        UpdateShopRequest request = new UpdateShopRequest("Mein Shop", "Aktualisierte Beschreibung!", "new-logo.png", "new-header.png");

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.findBySellerId("seller-1")).thenReturn(Optional.of(existingShop));
        when(shopRepository.save(any(Shop.class))).thenReturn(existingShop);

        shopService.updateCurrentSellerShop(request);

        assertThat(existingShop.getDescription()).isEqualTo("Aktualisierte Beschreibung!");
        assertThat(existingShop.getLogoUrl()).isEqualTo("new-logo.png");
        assertThat(existingShop.getHeaderImageUrl()).isEqualTo("new-header.png");
        assertThat(existingShop.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateCurrentSellerShop_throwsIllegalState_whenSellerHasNoShop() {
        Seller seller = buildSeller("seller-2", null);
        UpdateShopRequest request = new UpdateShopRequest("Mein Shop", "Neue Beschreibung hier", null, null);

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopRepository.findBySellerId("seller-2")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.updateCurrentSellerShop(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Der Shop des Händlers wurde nicht gefunden");

        verify(shopRepository, never()).save(any());
    }
    // ─── getActiveShopIds ────────────────────────────────────────────────────

    @Test
    void getActiveShopIds_returnsListOfIds_whenActiveShopsExist() {
        Shop shop1 = buildActiveShop("shop-1", "seller-1");
        Shop shop2 = buildActiveShop("shop-2", "seller-2");
        when(shopRepository.findByStatus(ShopStatus.ACTIVE)).thenReturn(List.of(shop1, shop2));

        List<String> result = shopService.getActiveShopIds();

        assertThat(result).containsExactlyInAnyOrder("shop-1", "shop-2");
        verify(shopRepository).findByStatus(ShopStatus.ACTIVE);
    }

    @Test
    void getActiveShopIds_returnsEmptyList_whenNoActiveShopsExist() {
        when(shopRepository.findByStatus(ShopStatus.ACTIVE)).thenReturn(List.of());

        assertThat(shopService.getActiveShopIds()).isEmpty();
    }

// ─── isShopActive ────────────────────────────────────────────────────────

    @Test
    void isShopActive_returnsTrue_whenShopStatusIsActive() {
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(buildActiveShop("shop-1", "seller-1")));
        assertThat(shopService.isShopActive("shop-1")).isTrue();
    }

    @Test
    void isShopActive_returnsFalse_whenShopStatusIsDraft() {
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(buildShop("shop-1", "seller-1")));
        assertThat(shopService.isShopActive("shop-1")).isFalse();
    }

    @Test
    void isShopActive_returnsFalse_whenShopNotFound() {
        when(shopRepository.findById("missing")).thenReturn(Optional.empty());
        assertThat(shopService.isShopActive("missing")).isFalse();
    }

// ─── getActiveShops ──────────────────────────────────────────────────────

    @Test
    void getActiveShops_returnsPageOfActiveShops() {
        Pageable pageable = PageRequest.of(0, 20);
        when(shopRepository.findByStatus(ShopStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(
                        buildActiveShop("shop-1", "seller-1"),
                        buildActiveShop("shop-2", "seller-2"))));

        Page<ShopResponse> result = shopService.getActiveShops(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo("shop-1");
    }

    @Test
    void getActiveShops_returnsEmptyPage_whenNoActiveShops() {
        Pageable pageable = PageRequest.of(0, 20);
        when(shopRepository.findByStatus(ShopStatus.ACTIVE, pageable)).thenReturn(Page.empty());

        assertThat(shopService.getActiveShops(pageable).getContent()).isEmpty();
    }

// ─── getActiveShopById ───────────────────────────────────────────────────

    @Test
    void getActiveShopById_returnsShopResponse_whenShopIsActive() {
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(buildActiveShop("shop-1", "seller-1")));

        ShopResponse result = shopService.getActiveShopById("shop-1");

        assertThat(result.id()).isEqualTo("shop-1");
        assertThat(result.status()).isEqualTo(ShopStatus.ACTIVE);
    }

    @Test
    void getActiveShopById_throwsShopNotFoundException_whenShopNotFound() {
        when(shopRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.getActiveShopById("missing"))
                .isInstanceOf(ShopNotFoundException.class)
                .hasMessageContaining("Shop nicht gefunden");
    }

    @Test
    void getActiveShopById_throwsShopNotFoundException_whenShopIsNotActive() {
        when(shopRepository.findById("shop-draft")).thenReturn(Optional.of(buildShop("shop-draft", "seller-1")));

        assertThatThrownBy(() -> shopService.getActiveShopById("shop-draft"))
                .isInstanceOf(ShopNotFoundException.class)
                .hasMessageContaining("nicht öffentlich verfügbar");
    }

// ─── getActiveShopBySlug ─────────────────────────────────────────────────

    @Test
    void getActiveShopBySlug_returnsShopResponse_whenSlugExistsAndShopIsActive() {
        when(shopRepository.findBySlug("aktiver-shop"))
                .thenReturn(Optional.of(buildActiveShop("shop-1", "seller-1")));

        ShopResponse result = shopService.getActiveShopBySlug("aktiver-shop");

        assertThat(result.slug()).isEqualTo("aktiver-shop");
        assertThat(result.status()).isEqualTo(ShopStatus.ACTIVE);
        verify(shopRepository).findBySlug("aktiver-shop");
    }

    @Test
    void getActiveShopBySlug_throwsShopNotFoundException_whenSlugNotFound() {
        when(shopRepository.findBySlug("unbekannt")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.getActiveShopBySlug("unbekannt"))
                .isInstanceOf(ShopNotFoundException.class)
                .hasMessageContaining("Shop nicht gefunden");
    }

    @Test
    void getActiveShopBySlug_throwsShopNotFoundException_whenShopIsNotActive() {
        when(shopRepository.findBySlug("mein-shop"))
                .thenReturn(Optional.of(buildShop("shop-draft", "seller-1")));

        assertThatThrownBy(() -> shopService.getActiveShopBySlug("mein-shop"))
                .isInstanceOf(ShopNotFoundException.class)
                .hasMessageContaining("nicht öffentlich verfügbar");
    }
}

