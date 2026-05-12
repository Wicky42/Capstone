import { type FC, useEffect, useState } from "react";
import type { Product } from "../types/product";
import type { Shop } from "../types/shop";
import { storefrontService } from "../services/storefrontService";
import ProductCard from "../components/product/ProductCard";
import ProductCardSkeleton from "../components/product/ProductCardSkeleton";
import ShopCard from "../components/seller/ShopCard";
import ShopCardSkeleton from "../components/seller/ShopCardSkeleton";
import SearchBar from "../components/base/SearchBar";
import CategoryFilterBar from "../components/base/CategoryFilterBar";
import "./StorefrontPage.css";

type Tab = "products" | "shops";

const PAGE_SIZE = 12;
const SKELETON_COUNT = 6;

const StorefrontPage: FC = () => {
    const [activeTab, setActiveTab] = useState<Tab>("products");

    // Products state
    const [products, setProducts] = useState<Product[]>([]);
    const [productPage, setProductPage] = useState(0);
    const [productTotalPages, setProductTotalPages] = useState(1);
    const [productLoading, setProductLoading] = useState(true);
    const [productLoadingMore, setProductLoadingMore] = useState(false);
    const [query, setQuery] = useState("");
    const [category, setCategory] = useState<string | undefined>(undefined);

    // Shops state
    const [shops, setShops] = useState<Shop[]>([]);
    const [shopPage, setShopPage] = useState(0);
    const [shopTotalPages, setShopTotalPages] = useState(1);
    const [shopLoading, setShopLoading] = useState(false);
    const [shopLoadingMore, setShopLoadingMore] = useState(false);

    // Load products (reset on filter/query change)
    useEffect(() => {
        setProducts([]);
        setProductPage(0);
        setProductLoading(true);

        storefrontService
            .getStorefrontProducts({ query: query || undefined, category, page: 0, size: PAGE_SIZE })
            .then((res) => {
                setProducts(res.content);
                setProductTotalPages(res.totalPages);
            })
            .catch(console.error)
            .finally(() => setProductLoading(false));
    }, [query, category]);

    // Load shops on tab switch
    useEffect(() => {
        if (activeTab !== "shops" || shops.length > 0) return;
        setShopLoading(true);
        storefrontService
            .getStorefrontShops(0, PAGE_SIZE)
            .then((res) => {
                setShops(res.content);
                setShopTotalPages(res.totalPages);
                setShopPage(0);
            })
            .catch(console.error)
            .finally(() => setShopLoading(false));
    }, [activeTab]);

    const loadMoreProducts = async () => {
        const nextPage = productPage + 1;
        setProductLoadingMore(true);
        try {
            const res = await storefrontService.getStorefrontProducts({
                query: query || undefined,
                category,
                page: nextPage,
                size: PAGE_SIZE,
            });
            setProducts((prev) => [...prev, ...res.content]);
            setProductPage(nextPage);
        } catch (e) {
            console.error(e);
        } finally {
            setProductLoadingMore(false);
        }
    };

    const loadMoreShops = async () => {
        const nextPage = shopPage + 1;
        setShopLoadingMore(true);
        try {
            const res = await storefrontService.getStorefrontShops(nextPage, PAGE_SIZE);
            setShops((prev) => [...prev, ...res.content]);
            setShopPage(nextPage);
        } catch (e) {
            console.error(e);
        } finally {
            setShopLoadingMore(false);
        }
    };

    return (
        <div className="storefront-page page">
            <div className="container">
                <section className="storefront-page__hero section">
                    <p className="section-kicker">Entdecke handgemachte Produkte</p>
                    <h1 className="section-title">Marktplatz</h1>
                    <p className="section-description">
                        Regionale Produkte aus kleinen Manufakturen – ehrlich, natürlich, handverlesen.
                    </p>
                </section>

                <div className="storefront-page__tabs">
                    <button
                        className={`storefront-page__tab${activeTab === "products" ? " storefront-page__tab--active" : ""}`}
                        onClick={() => setActiveTab("products")}
                        type="button"
                    >
                        Produkte
                    </button>
                    <button
                        className={`storefront-page__tab${activeTab === "shops" ? " storefront-page__tab--active" : ""}`}
                        onClick={() => setActiveTab("shops")}
                        type="button"
                    >
                        Shops
                    </button>
                </div>

                {activeTab === "products" && (
                    <>
                        <div className="storefront-page__filters">
                            <SearchBar value={query} onChange={setQuery} />
                            <CategoryFilterBar selected={category} onChange={setCategory} />
                        </div>

                        {productLoading ? (
                            <div className="product-grid">
                                {Array.from({ length: SKELETON_COUNT }).map((_, i) => (
                                    <ProductCardSkeleton key={i} />
                                ))}
                            </div>
                        ) : products.length === 0 ? (
                            <p className="storefront-page__empty">Keine Produkte gefunden.</p>
                        ) : (
                            <div className="product-grid">
                                {products.map((p) => (
                                    <ProductCard key={p.id} product={p} />
                                ))}
                            </div>
                        )}

                        {!productLoading && productPage + 1 < productTotalPages && (
                            <div className="storefront-page__load-more">
                                <button
                                    className="button-secondary"
                                    onClick={loadMoreProducts}
                                    disabled={productLoadingMore}
                                    type="button"
                                >
                                    {productLoadingMore ? "Laden…" : "Mehr laden"}
                                </button>
                            </div>
                        )}
                    </>
                )}

                {activeTab === "shops" && (
                    <>
                        {shopLoading ? (
                            <div className="shop-grid">
                                {Array.from({ length: SKELETON_COUNT }).map((_, i) => (
                                    <ShopCardSkeleton key={i} />
                                ))}
                            </div>
                        ) : shops.length === 0 ? (
                            <p className="storefront-page__empty">Keine Shops gefunden.</p>
                        ) : (
                            <div className="shop-grid">
                                {shops.map((s) => (
                                    <ShopCard key={s.id} shop={s} />
                                ))}
                            </div>
                        )}

                        {!shopLoading && shopPage + 1 < shopTotalPages && (
                            <div className="storefront-page__load-more">
                                <button
                                    className="button-secondary"
                                    onClick={loadMoreShops}
                                    disabled={shopLoadingMore}
                                    type="button"
                                >
                                    {shopLoadingMore ? "Laden…" : "Mehr laden"}
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
};

export default StorefrontPage;

