import { type FC, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useCartContext } from "../../context/CartContext";
import { storefrontService } from "../../services/storefrontService";
import CartItemRow from "../../components/cart/CartItemRow";
import "./CartPage.css";

const CartPage: FC = () => {
    const { items, totalItems, updateQuantity, removeItem } = useCartContext();
    // productId -> true wenn nicht mehr verfügbar
    const [unavailableIds, setUnavailableIds] = useState<Set<string>>(new Set());

    // Produktstatus für alle Items prüfen
    useEffect(() => {
        if (items.length === 0) return;

        const checkAvailability = async () => {
            const results = await Promise.allSettled(
                items.map((item) => storefrontService.getProductById(item.productId))
            );
            const newUnavailable = new Set<string>();
            results.forEach((result, index) => {
                if (
                    result.status === "rejected" ||
                    result.value.status !== "ACTIVE"
                ) {
                    newUnavailable.add(items[index].productId);
                }
            });
            setUnavailableIds(newUnavailable);
        };

        void checkAvailability();
    }, [items.length]); // nur neu prüfen wenn sich die Item-Anzahl ändert

    const activeItems = items.filter((i) => !unavailableIds.has(i.productId));
    const totalPrice = activeItems
        .reduce((sum, i) => sum + i.unitPrice * i.quantity, 0)
        .toLocaleString("de-DE", { style: "currency", currency: "EUR" });

    // Leer-Zustand
    if (totalItems === 0) {
        return (
            <div className="page cart-page">
                <div className="container cart-page__empty">
                    <svg className="cart-page__empty-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
                         fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden="true">
                        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
                        <line x1="3" y1="6" x2="21" y2="6" />
                        <path d="M16 10a4 4 0 0 1-8 0" />
                    </svg>
                    <h1 className="cart-page__empty-title">Dein Warenkorb ist leer</h1>
                    <p className="cart-page__empty-text">
                        Entdecke unsere handgemachten Produkte und füge sie hier hinzu.
                    </p>
                    <Link to="/" className="button-primary cart-page__empty-cta">
                        Zum Marktplatz
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="page cart-page">
            <div className="container">
                <h1 className="cart-page__title">Warenkorb</h1>

                <div className="cart-page__layout">
                    {/* Item-Liste */}
                    <section className="cart-page__items">
                        {items.map((item) => (
                            <CartItemRow
                                key={item.productId}
                                item={item}
                                isUnavailable={unavailableIds.has(item.productId)}
                                onUpdateQuantity={updateQuantity}
                                onRemove={removeItem}
                            />
                        ))}
                    </section>

                    {/* Zusammenfassung */}
                    <aside className="cart-page__summary">
                        <h2 className="cart-page__summary-title">Zusammenfassung</h2>

                        <div className="cart-page__summary-row">
                            <span>Artikel ({activeItems.reduce((s, i) => s + i.quantity, 0)})</span>
                            <strong>{totalPrice}</strong>
                        </div>

                        {unavailableIds.size > 0 && (
                            <p className="cart-page__summary-warning">
                                {unavailableIds.size} Produkt{unavailableIds.size > 1 ? "e sind" : " ist"} nicht mehr
                                verfügbar und wird beim Checkout entfernt.
                            </p>
                        )}

                        <div className="cart-page__summary-total">
                            <span>Gesamtpreis</span>
                            <strong>{totalPrice}</strong>
                        </div>

                        <button
                            className="button-primary cart-page__checkout-btn"
                            disabled
                            title="Checkout kommt in Phase 6"
                        >
                            Zur Kasse →
                        </button>

                        <Link to="/" className="cart-page__continue-link">
                            ← Weiter einkaufen
                        </Link>
                    </aside>
                </div>
            </div>
        </div>
    );
};

export default CartPage;

