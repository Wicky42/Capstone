import { useEffect, useState } from "react";
import { getMyShop } from "../../service/shopService.ts";
import type { Shop } from "../../types/Shop";

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
        return <p>Shop wird geladen ...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!shop) {
        return <p>Kein Shop gefunden.</p>;
    }

    return (
        <section>
            <h2>Dein Shop</h2>

            <p>
                <strong>Name:</strong> {shop.name}
            </p>

            <p>
                <strong>Beschreibung:</strong> {shop.description}
            </p>

            <p>
                <strong>Status:</strong> {shop.status}
            </p>

            <p>
                <strong>Slug:</strong> {shop.slug}
            </p>

            {shop.logoUrl && (
                <p>
                    <strong>Logo:</strong> {shop.logoUrl}
                </p>
            )}

            {shop.headerImageUrl && (
                <p>
                    <strong>Headerbild:</strong> {shop.headerImageUrl}
                </p>
            )}
        </section>
    );
}