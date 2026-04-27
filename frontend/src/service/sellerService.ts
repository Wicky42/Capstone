import axios from "axios";
import type { OnboardingStatus } from "../types/Onboarding";

export async function getOnboardingStatus(): Promise<OnboardingStatus> {
    const response = await axios.get("/api/seller/onboarding/status");
    return response.data;
}