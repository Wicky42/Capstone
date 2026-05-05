export type OnboardingStep =
    | "START"
    | "SHOP_CREATION"
    | "SHOP_CONFIGURATION"
    | "PRODUCT_CREATION"
    | "COMPLETED";

export type OnboardingStatus = {
    shopCreated: boolean;
    shopDataCompleted: boolean;      // Backend: shopDataCompleted
    firstProductCreated: boolean;
    onBoardingCompleted: boolean;    // Backend: onBoardingCompleted
    currentStep: OnboardingStep;
    nextStep: OnboardingStep;
    message: string;
};