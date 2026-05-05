import { useEffect, useState } from "react";
import OnboardingStatusCard from "../../components/seller/OnboardingStatusCard";
import CreateShopForm from "../../components/seller/CreateShopForm";
import SetSellerDataForm from "../../components/seller/SetSellerDataForm";
import ShopOverviewCard from "../../components/seller/ShopOverviewCard";
import AddProductPlaceholder from "../../components/seller/AddProductPlaceholder";
import { getOnboardingStatus } from "../../service/sellerService.ts";
import type { OnboardingStatus } from "../../types/Onboarding";
import "../../styles/pages/SellerOnboardingPage.css";

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
        return <div className="seller-onboarding-page__loading">Onboarding wird geladen ...</div>;
    }

    if (error) {
        return <div className="seller-onboarding-page__error">{error}</div>;
    }

    if (!status) {
        return <div className="seller-onboarding-page__error">Kein Onboarding-Status verfügbar.</div>;
    }

    return (
        <div className="seller-onboarding-page">
            <aside className="seller-onboarding-page__sidebar">
                <OnboardingStatusCard status={status} />
            </aside>

            <main className="seller-onboarding-page__content">
                <h1 className="seller-onboarding-page__title">Seller Onboarding</h1>

                {!status.shopCreated && (
                    <CreateShopForm onSuccess={loadStatus} />
                )}

                {status.shopCreated && !status.shopDataCompleted && (
                    <>
                        <ShopOverviewCard />
                        <SetSellerDataForm onSuccess={loadStatus} />
                    </>
                )}

                {status.shopCreated && status.shopDataCompleted && !status.firstProductCreated && (
                    <>
                        <ShopOverviewCard />
                        <AddProductPlaceholder />
                    </>
                )}

                {status.shopCreated && status.shopDataCompleted && status.firstProductCreated && (
                    <ShopOverviewCard />
                )}
            </main>
        </div>
    );
}