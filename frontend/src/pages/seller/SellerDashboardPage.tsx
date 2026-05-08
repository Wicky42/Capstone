import { useNavigate } from "react-router-dom";
import SellerDashboardStats from "../../components/seller/SellerDashboardStats";
import "./SellerDashboardPage.css";

export default function SellerDashboardPage() {
    const navigate = useNavigate();

    return (
        <div className="seller-dashboard">
            {/* Header */}
            <header className="seller-dashboard__header">
                <div>
                    <h1 className="seller-dashboard__title">Dashboard</h1>
                    <p className="seller-dashboard__subtitle">
                        Willkommen zurück – hier ist eine Übersicht deines Shops.
                    </p>
                </div>
                <div className="seller-dashboard__cta-group">
                    <button
                        className="button-primary"
                        onClick={() => navigate("/seller/products/new")}
                    >
                        Produkt hinzufügen
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

            <SellerDashboardStats />
        </div>
    );
}
