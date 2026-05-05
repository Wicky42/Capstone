import { useEffect, useState } from "react";
import { getMyShop } from "../../service/shopService.ts";
import type { Shop } from "../../types/Shop";
import "../../styles/components/seller/ShopOverviewCard.css";

export default function ShopOverviewCard() {
    const [shop, setShop] = useState<Shop | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    async function loadShop() {
        try {
            setIsLoading(true);
            setError(null);

            const data = await getMyShop();
            setShop(data);
        } catch (err) {
            setError("Dein Shop konnte nicht geladen werden.");
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        loadShop();
    }, []);

    if (isLoading) {
        return <p className="shop-overview-card__loading">Shop wird geladen …</p>;
    }

    if (error) {
        return <p className="shop-overview-card__error">{error}</p>;
    }

    if (!shop) {
        return <p className="shop-overview-card__empty">Kein Shop gefunden.</p>;
    }

    return (
        <section className="shop-overview-card">
            <h2 className="shop-overview-card__title">Dein Shop</h2>

            <hr className="shop-overview-card__divider" />

            <div className="shop-overview-card__row">
                <strong>Name</strong>
                <span>{shop.name}</span>
            </div>

            <div className="shop-overview-card__row">
                <strong>Beschreibung</strong>
                <p>{shop.description}</p>
            </div>

            <div className="shop-overview-card__row">
                <strong>Status</strong>
                <span
                    className={`shop-overview-card__status${
                        shop.status?.toLowerCase() !== "active" ? " shop-overview-card__status--inactive" : ""
                    }`}
                >
                    {shop.status}
                </span>
            </div>

            <div className="shop-overview-card__row">
                <strong>Slug</strong>
                <span>{shop.slug}</span>
            </div>

            {shop.logoUrl && (
                <div className="shop-overview-card__row">
                    <strong>Logo-URL</strong>
                    <a className="shop-overview-card__link" href={shop.logoUrl} target="_blank" rel="noreferrer">
                        {shop.logoUrl}
                    </a>
                </div>
            )}

            {shop.headerImageUrl && (
                <div className="shop-overview-card__row">
                    <strong>Header-Bild-URL</strong>
                    <a className="shop-overview-card__link" href={shop.headerImageUrl} target="_blank" rel="noreferrer">
                        {shop.headerImageUrl}
                    </a>
                </div>
            )}
        </section>
    );
}