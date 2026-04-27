import axios from "axios";
import type { OnboardingStatus } from "../types/Onboarding";
import type { SellerData } from "../types/SellerData";

const api = axios.create({
    baseURL: "/api",
    withCredentials: true,
});

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