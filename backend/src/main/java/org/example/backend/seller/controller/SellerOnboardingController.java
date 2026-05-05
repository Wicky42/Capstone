package org.example.backend.seller.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.seller.dto.OnboardingStatusResponse;
import org.example.backend.seller.service.SellerOnboardingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/onboarding")
@RequiredArgsConstructor
public class SellerOnboardingController {

    private final SellerOnboardingService sellerOnboardingService;

    @GetMapping("/status")
    public OnboardingStatusResponse getOnboardingStatus() {
        return sellerOnboardingService.getCurrentOnBoardingStatus();
    }
}
