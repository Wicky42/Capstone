import { type FC, useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import type { Shop } from "../../types/shop";
import type { Product } from "../../types/product";
import { storefrontService } from "../../services/storefrontService";
import ProductCard from "../../components/product/ProductCard";
import ProductCardSkeleton from "../../components/product/ProductCardSkeleton";
import "./ShopDetailPage.css";

const PAGE_SIZE = 12;
const SKELETON_COUNT = 6;

const ShopDetailPage: FC = () => {
    const { slug } = useParams<{ slug: string }>();
    const [shop, setShop] = useState<Shop | null>(null);
    const [products, setProducts] = useState<Product[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [shopLoading, setShopLoading] = useState(true);
    const [productsLoading, setProductsLoading] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!slug) return;

        const load = async () => {
            setShopLoading(true);
            try {
                const s = await storefrontService.getShopBySlug(slug);
                setShop(s);
                setProductsLoading(true);
                try {
                    const res = await storefrontService.getShopProducts(s.id, 0, PAGE_SIZE);
                    setProducts(res.content);
                    setTotalPages(res.totalPages);
                    setPage(0);
                } catch {
                    // leere Produktliste
                } finally {
                    setProductsLoading(false);
                }
            } catch {
                setError("Shop nicht gefunden.");
            } finally {
                setShopLoading(false);
            }
        };

        void load();
    }, [slug]);

    const loadMore = async () => {
        if (!shop) return;
        const nextPage = page + 1;
        setLoadingMore(true);
        try {
            const res = await storefrontService.getShopProducts(shop.id, nextPage, PAGE_SIZE);
            setProducts((prev) => [...prev, ...res.content]);
            setPage(nextPage);
        } catch (e) {
            console.error(e);
        } finally {
            setLoadingMore(false);
        }
    };

    if (shopLoading) return <div className="container shop-detail__loading">Laden…</div>;
    if (error || !shop) return (
        <div className="container shop-detail__error">
            <p>{error ?? "Shop nicht gefunden."}</p>
            <Link to="/" className="button-secondary">← Zurück zum Marktplatz</Link>
        </div>
    );

    return (
        <div className="shop-detail page">
            <div className="container">
                <nav className="shop-detail__breadcrumb">
                    <Link to="/">Marktplatz</Link>
                    <span> / {shop.name}</span>
                </nav>

                <header className="shop-detail__header">
                    {shop.headerImageUrl && (
                        <img
                            src={shop.headerImageUrl}
                            alt={`${shop.name} Header`}
                            className="shop-detail__header-img"
                        />
                    )}
                    <div className="shop-detail__header-content">
                        <div className="shop-detail__logo">
                            {shop.logoUrl ? (
                                <img src={shop.logoUrl} alt={`${shop.name} Logo`} />
                            ) : (
                                <span>{shop.name.charAt(0).toUpperCase()}</span>
                            )}
                        </div>
                        <div>
                            <h1 className="shop-detail__name">{shop.name}</h1>
                            <p className="shop-detail__description">{shop.description}</p>
                        </div>
                    </div>
                </header>

                <section className="shop-detail__products section">
                    <h2 className="shop-detail__section-title">Produkte</h2>

                    {productsLoading ? (
                        <div className="product-grid">
                            {Array.from({ length: SKELETON_COUNT }).map((_, i) => (
                                <ProductCardSkeleton key={i} />
                            ))}
                        </div>
                    ) : products.length === 0 ? (
                        <p className="shop-detail__empty">Dieser Shop hat noch keine aktiven Produkte.</p>
                    ) : (
                        <div className="product-grid">
                            {products.map((p) => (
                                <ProductCard key={p.id} product={p} />
                            ))}
                        </div>
                    )}

                    {!productsLoading && page + 1 < totalPages && (
                        <div className="shop-detail__load-more">
                            <button
                                className="button-secondary"
                                onClick={loadMore}
                                disabled={loadingMore}
                                type="button"
                            >
                                {loadingMore ? "Laden…" : "Mehr laden"}
                            </button>
                        </div>
                    )}
                </section>
            </div>
        </div>
    );
};

export default ShopDetailPage;

