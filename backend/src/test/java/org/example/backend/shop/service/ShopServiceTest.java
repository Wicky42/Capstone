package org.example.backend.shop.service;

import org.example.backend.seller.service.SellerService;
import org.example.backend.shop.dto.CreateShopRequest;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.model.Shop;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
}

