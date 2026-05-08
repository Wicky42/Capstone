import { useEffect, useState } from "react";
import { getMyShop } from "../../services/shopService";
import { productService } from "../../services/productService";
import type { Shop } from "../../types/shop";
import type { Product } from "../../types/product";
import "./ShopOverviewCard.css";

const STATUS_LABELS: Record<string, string> = {
    ACTIVE: "Aktiv",
    INACTIVE: "Inaktiv",
    DRAFT: "Entwurf",
};

function productStatusClass(status: string) {
    if (status === "INACTIVE") return " shop-overview-card__product-status--inactive";
    if (status === "DRAFT") return " shop-overview-card__product-status--draft";
    return "";
}

export default function ShopOverviewCard() {
    const [shop, setShop] = useState<Shop | null>(null);
    const [products, setProducts] = useState<Product[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    async function loadShop() {
        try {
            setIsLoading(true);
            setError(null);

            const [data, productPage] = await Promise.all([
                getMyShop(),
                productService.getSellerProducts(),
            ]);
            setShop(data);
            setProducts(productPage.content);
        } catch (_err) {
            setError("Dein Shop konnte nicht geladen werden.");
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        loadShop();
    }, []);

    // ...existing code...

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

            <hr className="shop-overview-card__divider" />

            <h3 className="shop-overview-card__products-title">Produkte</h3>

            {products.length === 0 ? (
                <p className="shop-overview-card__products-empty">Noch keine Produkte vorhanden.</p>
            ) : (
                <ul className="shop-overview-card__product-list">
                    {products.map((product) => (
                        <li key={product.id} className="shop-overview-card__product-item">
                            {product.imageUrl && (
                                <img
                                    className="shop-overview-card__product-image"
                                    src={`/api/seller/products/${product.id}/image`}
                                    alt={product.name}
                                    onError={(e) => {
                                        (e.currentTarget as HTMLImageElement).style.display = "none";
                                    }}
                                />
                            )}
                            <div className="shop-overview-card__product-body">
                                <div className="shop-overview-card__product-header">
                                    <p className="shop-overview-card__product-name">{product.name}</p>
                                    <span className={`shop-overview-card__product-status${productStatusClass(product.status)}`}>
                                        {STATUS_LABELS[product.status] ?? product.status}
                                    </span>
                                </div>
                                <p className="shop-overview-card__product-description">{product.description}</p>
                                <div className="shop-overview-card__product-meta">
                                    <span>Preis: {product.price.toFixed(2)} €</span>
                                    <span>Kategorie: {product.category}</span>
                                    <span>Bestand: {product.stockQuantity}</span>
                                </div>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </section>
    );
}