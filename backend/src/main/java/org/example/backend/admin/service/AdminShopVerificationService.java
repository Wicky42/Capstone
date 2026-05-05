package org.example.backend.admin.service;

import lombok.RequiredArgsConstructor;
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
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminShopVerificationService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    public List<PendingShopVerificationResponse> getPendingShopVerifications(){
        List<Shop> draftShops = shopRepository.findByStatus(ShopStatus.DRAFT);

        return draftShops.stream()
                .map(shop -> {
                    // Seller ermitteln
                    User user = userRepository.findById(shop.getSellerId()).orElse(null);
                    if (!(user instanceof Seller seller) || !seller.isOnboardingCompleted()) {
                        return null;
                    }
                    if(!productRepository.existsByShopId(shop.getId())){
                        return null;
                    }
                    return PendingShopVerificationResponse.builder()
                            .shopId(shop.getId())
                            .shopName(shop.getName())
                            .shopDescription(shop.getDescription())
                            .sellerId(seller.getId())
                            .sellerName(seller.getBusinessName())
                            .sellerEmail(seller.getEmail())
                            .shopStatus(shop.getStatus().name())
                            .onboardingCompleted(seller.isOnboardingCompleted())
                            .createdAt(shop.getCreatedAt())
                            .updatedAt(shop.getUpdatedAt())
                            .build();
                })
                .filter(response -> response != null)
                .toList();
    }

    public ShopResponse activateShop(String shopId) {
        Shop shop = getShopOrThrow(shopId);
        Seller seller = getSellerForShopOrThrow(shop);

        validateShopCanBeActivated(shop, seller);

        shop.setStatus(ShopStatus.ACTIVE);
        shop.setUpdatedAt(LocalDateTime.now());
        Shop savedShop = shopRepository.save(shop);

        return ShopResponse.from(savedShop);
    }

    public ShopResponse deactivateShop(String shopId){
        Shop shop = getShopOrThrow(shopId);
        Seller seller = getSellerForShopOrThrow(shop);

        validateShopCanBeDeactivated(shop, seller);

        shop.setStatus(ShopStatus.INACTIVE);
        shop.setUpdatedAt(LocalDateTime.now());
        Shop savedShop = shopRepository.save(shop);

        return ShopResponse.from(savedShop);
    }

    public ShopResponse rejectShop(String shopId) {
        Shop shop = getShopOrThrow(shopId);

        validateShopCanBeRejected(shop);

        shop.setStatus(ShopStatus.REJECTED);
        shop.setUpdatedAt(LocalDateTime.now());
        Shop savedShop = shopRepository.save(shop);
        return ShopResponse.from(savedShop);
    }




    // ------------- Helper
    private void validateShopCanBeActivated(@NonNull Shop shop, Seller seller) {
        if (shop.getStatus() == ShopStatus.ACTIVE) {
            throw new ShopAlreadyActiveException("Shop ist bereits aktiviert");
        }

        if(shop.getStatus() == ShopStatus.REJECTED) {
            throw new ShopRejectedException("Shop wurde bereits abgelehnt und kann nicht mehr aktiviert werden");
        }

        if (!seller.isOnboardingCompleted()) {
            throw new SellerOnboardingIncompleteException("Onboarding ist noch nicht abgeschlossen");
        }

        if (!productRepository.existsByShopId(shop.getId())) {
            throw new ShopHasNoProductsException("Shop hat noch keine Produkte");
        }
    }

    private void validateShopCanBeDeactivated(Shop shop, Seller seller) {
        if (shop.getStatus() == ShopStatus.INACTIVE) {
            throw new ShopAlreadyInactiveException("Shop ist bereits deaktiviert");
        }
        if(shop.getStatus() == ShopStatus.REJECTED) {
            throw new ShopRejectedException("Shop wurde bereits abgelehnt und kann nicht mehr deaktiviert werden");
        }
        if(!seller.isOnboardingCompleted()){
            throw new SellerOnboardingIncompleteException("Onboarding ist noch nicht abgeschlossen");
        }
    }

    private void validateShopCanBeRejected(Shop shop){
        if(shop.getStatus() == ShopStatus.REJECTED) {
            throw new ShopRejectedException("Shop wurde bereits abgelehnt");
        }
        if (shop.getStatus() != ShopStatus.DRAFT) {
            throw new InvalidShopStateException("Nur Shops im Status DRAFT können abgelehnt werden");
        }
    }

    private Shop getShopOrThrow(String shopId){
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopNotFoundException("Shop mit ID: " + shopId +" wurde nicht gefunden"));
    }

    private Seller getSellerForShopOrThrow(Shop shop){
        User user = userRepository.findById(shop.getSellerId())
                .orElseThrow(() -> new UserNotFoundException(
                        "Der Shop mit ID: " + shop.getId() + " ist keinem Benutzer zugeordnet"
                ));

        if (!(user instanceof Seller shopSeller)) {
            throw new InvalidShopStateException(
                    "Der Shop mit ID: " + shop.getId() + " ist keinem gültigen Verkäufer zugeordnet"
            );
        }

        return shopSeller;
    }


}
