package org.example.backend.seller.service;

import org.example.backend.shop.model.Shop;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.user.model.Seller;
import org.example.backend.user.model.User;
import org.example.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SellerService sellerService;

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
                .slug("mein-shop")
                .status(ShopStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void linkShopToSeller_returnsTrue_andSavesSeller_whenSellerHasNoShop() {
        Seller seller = buildSeller("seller-1", null);
        Shop shop = buildShop("shop-1", "seller-1");

        boolean result = sellerService.linkShopToSeller(seller, shop);

        assertThat(result).isTrue();
        assertThat(seller.getShopId()).isEqualTo("shop-1");
        assertThat(seller.getUpdatedAt()).isNotNull();
        verify(userRepository).save(seller);
    }

    @Test
    void linkShopToSeller_returnsFalse_andDoesNotSave_whenSellerAlreadyHasShop() {
        Seller seller = buildSeller("seller-2", "existing-shop-id");
        Shop shop = buildShop("shop-2", "seller-2");

        boolean result = sellerService.linkShopToSeller(seller, shop);

        assertThat(result).isFalse();
        assertThat(seller.getShopId()).isEqualTo("existing-shop-id");
        verifyNoInteractions(userRepository);
    }

    @Test
    void linkShopToSeller_setsUpdatedAt_whenSuccessful() {
        Seller seller = buildSeller("seller-3", null);
        Shop shop = buildShop("shop-3", "seller-3");

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        sellerService.linkShopToSeller(seller, shop);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertThat(seller.getUpdatedAt()).isAfter(before).isBefore(after);
    }
}

