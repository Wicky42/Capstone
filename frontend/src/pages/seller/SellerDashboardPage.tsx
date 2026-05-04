import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMyShop } from "../../services/shopService";
import { getOnboardingStatus } from "../../services/sellerService";
import { productService } from "../../services/productService";
import type { Shop } from "../../types/shop";
import type { OnboardingStatus } from "../../types/onboarding";
import "./SellerDashboardPage.css";

type ProductCounts = {
    total: number;
    active: number;
    draft: number;
    inactive: number;
};

const SHOP_STATUS_LABEL: Record<string, string> = {
    ACTIVE: "Aktiv",
    DRAFT: "Entwurf",
    INACTIVE: "Inaktiv",
};

const SHOP_STATUS_CLASS: Record<string, string> = {
    ACTIVE: "status-badge--active",
    DRAFT: "status-badge--draft",
    INACTIVE: "status-badge--inactive",
};

const ONBOARDING_STEP_LABEL: Record<string, string> = {
    START: "Gestartet",
    SHOP_CREATION: "Shop erstellen",
    SHOP_CONFIGURATION: "Shop konfigurieren",
    PRODUCT_CREATION: "Produkt anlegen",
    COMPLETED: "Abgeschlossen",
};

export default function SellerDashboardPage() {
    const navigate = useNavigate();

    const [shop, setShop] = useState<Shop | null>(null);
    const [onboarding, setOnboarding] = useState<OnboardingStatus | null>(null);
    const [counts, setCounts] = useState<ProductCounts | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function loadDashboard() {
            try {
                setIsLoading(true);
                setError(null);

                const [shopData, onboardingData, totalData, activeData, draftData, inactiveData] =
                    await Promise.all([
                        getMyShop(),
                        getOnboardingStatus(),
                        productService.getSellerProducts({ size: 1 }),
                        productService.getSellerProducts({ size: 1, status: "ACTIVE" }),
                        productService.getSellerProducts({ size: 1, status: "DRAFT" }),
                        productService.getSellerProducts({ size: 1, status: "INACTIVE" }),
                    ]);

                setShop(shopData);
                setOnboarding(onboardingData);
                setCounts({
                    total: totalData.totalElements,
                    active: activeData.totalElements,
                    draft: draftData.totalElements,
                    inactive: inactiveData.totalElements,
                });
            } catch {
                setError("Das Dashboard konnte nicht geladen werden.");
            } finally {
                setIsLoading(false);
            }
        }

        loadDashboard();
    }, []);

    if (isLoading) {
        return <div className="seller-dashboard__state">Dashboard wird geladen …</div>;
    }

    if (error) {
        return <div className="seller-dashboard__state seller-dashboard__state--error">{error}</div>;
    }

    return (
        <div className="seller-dashboard">
            {/* Header */}
            <header className="seller-dashboard__header">
                <div>
                    <h1 className="seller-dashboard__title">Dashboard</h1>
                    {shop && (
                        <p className="seller-dashboard__subtitle">
                            Willkommen zurück – hier ist eine Übersicht deines Shops.
                        </p>
                    )}
                </div>
                <div className="seller-dashboard__cta-group">
                    <button
                        className="button-primary"
                        onClick={() => navigate("/seller/products/new")}
                    >
                        + Produkt hinzufügen
                    </button>
                    <button
                        className="button-secondary"
                        onClick={() => navigate("/seller/products")}
                    >
                        Produkte verwalten
                    </button>
                    <button
                        className="button-secondary"
                        onClick={() => navigate("/seller/shop/edit")}
                    >
                        Shop bearbeiten
                    </button>
                </div>
            </header>

            {/* Shop-Infos */}
            {shop && (
                <section className="seller-dashboard__section">
                    <h2 className="seller-dashboard__section-title">Mein Shop</h2>
                    <div className="seller-dashboard__cards">
                        <div className="seller-dashboard__info-card card">
                            <span className="seller-dashboard__info-label">Shopname</span>
                            <span className="seller-dashboard__info-value">{shop.name}</span>
                        </div>
                        <div className="seller-dashboard__info-card card">
                            <span className="seller-dashboard__info-label">Shop-Status</span>
                            <span
                                className={`status-badge ${SHOP_STATUS_CLASS[shop.status] ?? ""}`}
                            >
                                {SHOP_STATUS_LABEL[shop.status] ?? shop.status}
                            </span>
                        </div>
                        {onboarding && (
                            <div className="seller-dashboard__info-card card">
                                <span className="seller-dashboard__info-label">Onboarding-Status</span>
                                <span
                                    className={`status-badge ${
                                        onboarding.onBoardingCompleted
                                            ? "status-badge--active"
                                            : "status-badge--draft"
                                    }`}
                                >
                                    {onboarding.onBoardingCompleted
                                        ? "Abgeschlossen"
                                        : ONBOARDING_STEP_LABEL[onboarding.currentStep] ??
                                          onboarding.currentStep}
                                </span>
                            </div>
                        )}
                    </div>
                </section>
            )}

            {/* Produkt-Statistiken */}
            {counts && (
                <section className="seller-dashboard__section">
                    <h2 className="seller-dashboard__section-title">Produkte</h2>
                    <div className="seller-dashboard__stat-grid">
                        <div className="seller-dashboard__stat-card card">
                            <span className="seller-dashboard__stat-value">{counts.total}</span>
                            <span className="seller-dashboard__stat-label">Produkte gesamt</span>
                        </div>
                        <div className="seller-dashboard__stat-card seller-dashboard__stat-card--active card">
                            <span className="seller-dashboard__stat-value">{counts.active}</span>
                            <span className="seller-dashboard__stat-label">Aktiv</span>
                        </div>
                        <div className="seller-dashboard__stat-card seller-dashboard__stat-card--draft card">
                            <span className="seller-dashboard__stat-value">{counts.draft}</span>
                            <span className="seller-dashboard__stat-label">Entwurf</span>
                        </div>
                        <div className="seller-dashboard__stat-card seller-dashboard__stat-card--inactive card">
                            <span className="seller-dashboard__stat-value">{counts.inactive}</span>
                            <span className="seller-dashboard__stat-label">Inaktiv</span>
                        </div>
                    </div>
                </section>
            )}
        </div>
    );
}

