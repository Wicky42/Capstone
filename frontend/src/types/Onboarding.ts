export type OnboardingStep =
    | "START"
    | "SHOP_CREATION"
    | "SHOP_CONFIGURATION"
    | "PRODUCT_CREATION"
    | "COMPLETED";
export type OnboardingStatus = {
    shopCreated: boolean;
    shopDataCompleted: boolean;
    firstProductCreated: boolean;
    onBoardingCompleted: boolean;
    currentStep: OnboardingStep;
    nextStep: OnboardingStep;
    message: string;
};
