package org.example.backend.seller.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.seller.dto.OnboardingStatusResponse;
import org.example.backend.seller.model.OnboardingStep;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.model.User;
import org.example.backend.user.repo.UserRepository;
import org.example.backend.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerOnboardingService {

    private final UserService userService;
    private final ShopRepository shopRepository;

    public OnboardingStatusResponse getCurrentOnBoardingStatus(){
        Seller seller = userService.getCurrentSeller();

        boolean shopCreated = seller.getShopId() != null && shopRepository.existsById(seller.getShopId());

        //First creat a Shop
        if(!shopCreated) {
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

        //Shop created, but shop data not completed
        //TODO: expand here when Phase 3 with Products
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


}
