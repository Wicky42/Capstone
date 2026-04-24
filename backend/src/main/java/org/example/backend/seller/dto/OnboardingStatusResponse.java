package org.example.backend.seller.dto;

import org.example.backend.seller.model.OnboardingSteps;

public record OnboardingStatusResponse(
        boolean shopCreated,
        boolean shopDataCompleted,
        boolean firstProductCreated,
        boolean onBoardingCompleted,
        OnboardingSteps currentStep,
        OnboardingSteps nextStep,
        String message
) {
}
