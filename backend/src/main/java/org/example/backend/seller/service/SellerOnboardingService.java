package org.example.backend.seller.service;

import lombok.RequiredArgsConstructor;
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

    public OnboardingStatusResponse getCurrentOnBoardingStatus(){
        Seller seller = userService.getCurrentSeller();

        //Nothing done - Seller should first creat a shop
        if(!hasShop(seller)) {
            return new OnboardingStatusResponse(
                    false,
                    false,
                    false,
                    false,
                    OnboardingStep.START,
                    OnboardingStep.SHOP_CREATION,
                    "Erstelle deinen Shop!"
            );
        }

        //Shop exists but data is not completed - Seller should complete shop data
        if(!isShopDataCompleted(seller)) {
            return new OnboardingStatusResponse(
                    true,
                    false,
                    false,
                    false,
                    OnboardingStep.SHOP_CREATION,
                    OnboardingStep.SHOP_CONFIGURATION,
                    "Vervollständige deine Shop-Daten."
            );
        }

        //Shop data completed, Need to set first product
        return new OnboardingStatusResponse(
                true,
                true,
                false,
                false,
                OnboardingStep.SHOP_CONFIGURATION,
                OnboardingStep.PRODUCT_CREATION,
                "Vervollständige deine Shop-Daten."
        );
        //TODO onboardning completed when product is set

    }

    /*--------------- HELPER -------------*/
    private boolean hasShop(Seller seller){
        return seller.getShopId() != null && shopRepository.existsById(seller.getShopId());
    }

    //Shop Data is completed, when name & description is set
    private boolean isShopDataCompleted(Seller seller){
        Shop sellerShop = shopRepository.findBySellerId(seller.getId())
                .orElseThrow(() -> new IllegalStateException("Shop nicht gefunden"));
        return sellerShop.getDescription() != null && !sellerShop.getDescription().isEmpty()
                && sellerShop.getName() != null && !sellerShop.getName().isEmpty();
    }


}
