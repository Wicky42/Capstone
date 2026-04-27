import api from "./api";
import type { OnboardingStatus } from "../types/Onboarding";
import type { SellerData } from "../types/SellerData";


export async function getOnboardingStatus(): Promise<OnboardingStatus> {
    const response = await api.get("/seller/onboarding/status");
    return response.data;
}

export async function getSellerProfile(): Promise<SellerData> {
    const response = await api.get("/seller/profile");
    return response.data;
}

export async function updateSellerProfile(data: SellerData): Promise<SellerData> {
    const response = await api.put("/seller/profile", data);
    return response.data;
}