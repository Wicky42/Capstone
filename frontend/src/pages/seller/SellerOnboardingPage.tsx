import { useEffect, useState } from "react";
import OnboardingStatusCard from "../../components/seller/OnboardingStatusCard";
import CreateShopForm from "../../components/seller/CreateShopForm";
import SetSellerDataForm from "../../components/seller/SetSellerDataForm";
import ShopOverviewCard from "../../components/seller/ShopOverviewCard";
import AddProductOnboardingForm from "../../components/seller/AddProductOnboardingForm";
import { getOnboardingStatus } from "../../services/sellerService";
import type { OnboardingStatus } from "../../types/onboarding";
import "./SellerOnboardingPage.css";

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
                    <SetSellerDataForm onSuccess={loadStatus} />
                )}

                {status.shopCreated && status.shopDataCompleted && !status.firstProductCreated && (
                    <AddProductOnboardingForm onSuccess={loadStatus}/>
                )}

                {status.shopCreated && status.shopDataCompleted && status.firstProductCreated && (
                    <>
                        <ShopOverviewCard />
                        <button>Aktiviere deinen Shop und starte den Verkauf!</button>
                    </>


                )}
            </main>
        </div>
    );
}