import { useEffect, useState } from "react";
import OnboardingStatusCard from "../../components/seller/OnboardingStatusCard";
import CreateShopForm from "../../components/seller/CreateShopForm";
import SetSellerDataForm from "../../components/seller/SetSellerDataForm";
import ShopOverviewCard from "../../components/seller/ShopOverviewCard";
import { getOnboardingStatus } from "../../service/sellerService.ts";
import type { OnboardingStatus } from "../../types/Onboarding";

export default function SellerOnboardingPage() {
    const [status, setStatus] = useState<OnboardingStatus | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    async function loadStatus() {
        try {
            setIsLoading(true);
            setError(null);

            const data = await getOnboardingStatus();
            setStatus(data);
        } catch {
            setError("Der Onboarding-Status konnte nicht geladen werden.");
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        loadStatus();
    }, []);

    if (isLoading) {
        return <p>Onboarding wird geladen ...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!status) {
        return <p>Kein Onboarding-Status verfügbar.</p>;
    }

    return (
        <div>
            <h1>Seller Onboarding</h1>

            <OnboardingStatusCard status={status} />

            {!status.shopCreated && (
                <CreateShopForm onSuccess={loadStatus} />
            )}

            {status.shopCreated && !status.shopDataComplete && (
                <SetSellerDataForm onSuccess={loadStatus} />
            )}

            {status.shopCreated && status.shopDataComplete && (
                <ShopOverviewCard />
            )}
        </div>
    );
}