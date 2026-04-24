package org.example.backend.seller.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.seller.dto.OnboardingStatusResponse;
import org.example.backend.seller.model.OnboardingStep;
import org.example.backend.shop.repository.ShopRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.model.User;
import org.example.backend.user.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerOnboardingService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public OnboardingStatusResponse getCurrentOnBoardingStatus(){
        Seller seller = getCurrentSeller();

        boolean shopCreated = seller.getShopId() != null && shopRepository.existsById(seller.getShopId());

        //First creat a Shop
        if(!shopCreated) {
            return new OnboardingStatusResponse(
                    false,
                    false,
                    false,
                    false,
                    OnboardingStep.START,
                    OnboardingStep.PRODUCT_CREATION,
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

    private Seller getCurrentSeller(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User appUser)) {
            throw new ForbiddenAccessException("Kein eingeloggter Benutzer gefunden.");
        }

        User currentUser = userRepository.findById(appUser.getId())
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));

        if(currentUser.getRole() != User.Role.SELLER){
            throw new ForbiddenAccessException("Unerlaubter Zugriff. Nur Seller dürfen diesen Bereich nutzen.");
        }
        //Wenn das vorkommt bitte ganze User Domaine überprüfen
        if (!(currentUser instanceof Seller seller)) {
            throw new IllegalStateException("Benutzer hat Rolle SELLER, ist aber kein Seller-Typ.");
        }
        return seller;
    }


}
