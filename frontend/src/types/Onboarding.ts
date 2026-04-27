export type OnboardingStep =
    | "START"
    | "SHOP_CREATION"
    | "SHOP_CONFIGURATION"
    | "PRODUCT_CREATION"
    | "COMPLETED";

export type OnboardingStatus = {
    shopCreated: boolean;
    shopDataComplete: boolean;
    firstProductCreated: boolean;
    onboardingCompleted: boolean;
    currentStep: OnboardingStep;
    nextStep: OnboardingStep;
    message: string;
};