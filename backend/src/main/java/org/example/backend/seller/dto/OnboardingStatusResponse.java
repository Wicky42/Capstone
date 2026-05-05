package org.example.backend.seller.dto;

import org.example.backend.seller.model.OnboardingStep;

public record OnboardingStatusResponse(
        boolean shopCreated,
        boolean shopDataCompleted,
        boolean firstProductCreated,
        boolean onBoardingCompleted,
        OnboardingStep currentStep,
        OnboardingStep nextStep,
        String message
) {
}
