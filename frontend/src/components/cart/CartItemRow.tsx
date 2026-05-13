import type { FC } from "react";
import { Link } from "react-router-dom";
import type { CartItem } from "../../types/cart";
import { storefrontService } from "../../services/storefrontService";
import "./CartItemRow.css";

type Props = {
    item: CartItem;
    isUnavailable: boolean;
    onUpdateQuantity: (productId: string, quantity: number) => void;
    onRemove: (productId: string) => void;
};

const CartItemRow: FC<Props> = ({ item, isUnavailable, onUpdateQuantity, onRemove }) => {
    const imageUrl = item.titleImage
        ? storefrontService.getProductImageUrl(item.productId)
        : null;

    const lineTotal = (item.unitPrice * item.quantity).toLocaleString("de-DE", {
        style: "currency",
        currency: "EUR",
    });

    return (
        <article className={`cart-item${isUnavailable ? " cart-item--unavailable" : ""}`}>
            <div className="cart-item__image-wrap">
                {imageUrl ? (
                    <img
                        src={imageUrl}
                        alt={item.productName}
                        className="cart-item__image"
                    />
                ) : (
                    <div className="cart-item__image-placeholder" aria-hidden="true" />
                )}
            </div>

            <div className="cart-item__details">
                <Link
                    to={`/products/${item.productId}`}
                    className="cart-item__name"
                    tabIndex={isUnavailable ? -1 : 0}
                >
                    {item.productName}
                </Link>

                <span className="cart-item__unit-price">
                    {item.unitPrice.toLocaleString("de-DE", { style: "currency", currency: "EUR" })} / Stück
                </span>

                {isUnavailable && (
                    <span className="cart-item__unavailable-badge">
                        Nicht mehr verfügbar
                    </span>
                )}
            </div>

            <div className="cart-item__controls">
                {!isUnavailable ? (
                    <div className="cart-item__stepper">
                        <button
                            className="cart-item__stepper-btn"
                            onClick={() => onUpdateQuantity(item.productId, item.quantity - 1)}
                            disabled={item.quantity <= 1}
                            aria-label="Menge verringern"
                        >
                            −
                        </button>
                        <span className="cart-item__qty">{item.quantity}</span>
                        <button
                            className="cart-item__stepper-btn"
                            onClick={() => onUpdateQuantity(item.productId, item.quantity + 1)}
                            disabled={item.quantity >= item.maxQuantity}
                            aria-label="Menge erhöhen"
                        >
                            +
                        </button>
                    </div>
                ) : (
                    <span className="cart-item__qty cart-item__qty--muted">{item.quantity}×</span>
                )}
            </div>

            <div className="cart-item__totals">
                <strong className="cart-item__line-total">{lineTotal}</strong>
                <button
                    className="cart-item__remove"
                    onClick={() => onRemove(item.productId)}
                    aria-label={`${item.productName} entfernen`}
                >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                         stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                         aria-hidden="true">
                        <polyline points="3 6 5 6 21 6" />
                        <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                        <path d="M10 11v6" />
                        <path d="M14 11v6" />
                        <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                    </svg>
                </button>
            </div>
        </article>
    );
};

export default CartItemRow;

