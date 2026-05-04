import { useCallback, useEffect, useState, useTransition } from "react";
import { useNavigate } from "react-router-dom";
import { productService } from "../../services/productService";
import type { Product } from "../../types/product";
import SellerProductTable from "../../components/seller/SellerProductTable";
import "./ProductListPage.css";

const PAGE_SIZE = 20;

export default function ProductListPage() {
    const navigate = useNavigate();

    const [products, setProducts] = useState<Product[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [page, setPage] = useState(0);
    const [error, setError] = useState<string | null>(null);
    const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);
    const [actionError, setActionError] = useState<string | null>(null);
    const [isPending, startTransition] = useTransition();

    const totalPages = Math.ceil(totalElements / PAGE_SIZE);

    const loadProducts = useCallback(async (pageIndex: number) => {
        try {
            setError(null);
            const data = await productService.getSellerProducts({
                page: pageIndex,
                size: PAGE_SIZE,
                sort: "name,asc",
            });
            setProducts(data.content);
            setTotalElements(data.totalElements);
        } catch {
            setError("Produkte konnten nicht geladen werden.");
        }
    }, []);

    useEffect(() => {
        startTransition(() => { loadProducts(page); });
    }, [page, loadProducts]);

    async function handlePublish(product: Product) {
        try {
            setActionLoadingId(product.id);
            setActionError(null);
            await productService.updateProduct(product.id, {
                name: product.name,
                description: product.description,
                price: product.price,
                category: product.category,
                productionDate: product.productionDate ?? null,
                bestBeforeDate: product.bestBeforeDate ?? null,
                stockQuantity: product.stockQuantity,
                status: "ACTIVE",
            });
            await loadProducts(page);
        } catch {
            setActionError(`„${product.name}" konnte nicht veröffentlicht werden.`);
        } finally {
            setActionLoadingId(null);
        }
    }

    async function handleDeactivate(product: Product) {
        try {
            setActionLoadingId(product.id);
            setActionError(null);
            await productService.updateProduct(product.id, {
                name: product.name,
                description: product.description,
                price: product.price,
                category: product.category,
                productionDate: product.productionDate ?? null,
                bestBeforeDate: product.bestBeforeDate ?? null,
                stockQuantity: product.stockQuantity,
                status: "INACTIVE",
            });
            await loadProducts(page);
        } catch {
            setActionError(`„${product.name}" konnte nicht deaktiviert werden.`);
        } finally {
            setActionLoadingId(null);
        }
    }

    const isLoading = isPending;

    return (
        <div className="product-list-page">
            {/* Header */}
            <header className="product-list-page__header">
                <div>
                    <h1 className="product-list-page__title">Meine Produkte</h1>
                    {!isLoading && (
                        <p className="product-list-page__count">
                            {totalElements} {totalElements === 1 ? "Produkt" : "Produkte"} gesamt
                        </p>
                    )}
                </div>
                <button
                    className="button-primary"
                    onClick={() => navigate("/seller/products/new")}
                >
                    + Neues Produkt anlegen
                </button>
            </header>

            {/* Aktions-Fehlermeldung */}
            {actionError && (
                <div className="product-list-page__action-error" role="alert">
                    {actionError}
                </div>
            )}

            {/* Lade-Zustand */}
            {isLoading && (
                <div className="product-list-page__state">Produkte werden geladen …</div>
            )}

            {/* Fehler-Zustand */}
            {!isLoading && error && (
                <div className="product-list-page__state product-list-page__state--error">
                    {error}
                </div>
            )}

            {/* Empty State */}
            {!isLoading && !error && products.length === 0 && (
                <div className="product-list-page__empty">
                    <div className="product-list-page__empty-icon" aria-hidden>📦</div>
                    <h2 className="product-list-page__empty-title">Noch keine Produkte</h2>
                    <p className="product-list-page__empty-text">
                        Du hast noch kein Produkt angelegt. Starte jetzt und füge dein
                        erstes Produkt hinzu.
                    </p>
                    <button
                        className="button-primary"
                        onClick={() => navigate("/seller/products/new")}
                    >
                        + Erstes Produkt anlegen
                    </button>
                </div>
            )}

            {/* Tabelle */}
            {!isLoading && !error && products.length > 0 && (
                <>
                    <SellerProductTable
                        products={products}
                        onPublish={handlePublish}
                        onDeactivate={handleDeactivate}
                        loadingId={actionLoadingId}
                    />

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="product-list-page__pagination">
                            <button
                                className="button-secondary"
                                onClick={() => setPage((p) => p - 1)}
                                disabled={page === 0}
                            >
                                ← Zurück
                            </button>
                            <span className="product-list-page__page-info">
                                Seite {page + 1} von {totalPages}
                            </span>
                            <button
                                className="button-secondary"
                                onClick={() => setPage((p) => p + 1)}
                                disabled={page >= totalPages - 1}
                            >
                                Weiter →
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}
