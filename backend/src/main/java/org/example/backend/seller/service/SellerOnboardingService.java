package org.example.backend.seller.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.product.repository.ProductRepository;
import org.example.backend.seller.dto.OnboardingStatusResponse;
import org.example.backend.seller.model.OnboardingStep;
import org.example.backend.shop.model.Shop;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerOnboardingService {

    private final UserService userService;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    public OnboardingStatusResponse getCurrentOnboardingStatus() {
        Seller seller = userService.getCurrentSeller();

        if (!hasShop(seller)) {
            return new OnboardingStatusResponse(
                    false,
                    false,
                    false,
                    false,
                    OnboardingStep.START,
                    OnboardingStep.SHOP_CONFIGURATION,
                    "Erstelle deinen Shop!"
            );
        }

        Shop shop = getSellerShop(seller);

        if (!isShopDataCompleted(shop)) {
            return new OnboardingStatusResponse(
                    true,
                    false,
                    false,
                    false,
                    OnboardingStep.SHOP_CONFIGURATION,
                    OnboardingStep.PRODUCT_CREATION,
                    "Vervollständige deine Shop-Daten."
            );
        }

        if (!isFirstProductUploaded(shop)) {
            return new OnboardingStatusResponse(
                    true,
                    true,
                    false,
                    false,
                    OnboardingStep.PRODUCT_CREATION,
                    OnboardingStep.COMPLETED,
                    "Füge dein erstes Produkt ein."
            );
        }

        return new OnboardingStatusResponse(
                true,
                true,
                true,
                true,
                OnboardingStep.COMPLETED,
                null,
                "Dein Shop ist bereit. Verwalte deine Produkte."
        );
    }

    private boolean hasShop(Seller seller) {
        return seller.getShopId() != null && shopRepository.existsById(seller.getShopId());
    }

    private Shop getSellerShop(Seller seller) {
        return shopRepository.findById(seller.getShopId())
                .orElseThrow(() -> new IllegalStateException("Shop nicht gefunden"));
    }

    private boolean isShopDataCompleted(Shop shop) {
        return hasText(shop.getName()) && hasText(shop.getDescription());
    }

    private boolean isFirstProductUploaded(Shop shop) {
        return productRepository.existsByShopId(shop.getId());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}