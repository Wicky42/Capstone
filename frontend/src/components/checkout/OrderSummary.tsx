import { type FC, useEffect, useState } from "react";
import type { CartItem } from "../../types/cart";
import type { Address } from "../../types/address";
import { storefrontService } from "../../services/storefrontService";
import "./OrderSummary.css";

type Props = {
    activeItems: CartItem[];
    shippingAddress: Address;
    billingAddress: Address;
};

const fmt = (n: number) => n.toLocaleString("de-DE", { style: "currency", currency: "EUR" });

const OrderSummary: FC<Props> = ({ activeItems, shippingAddress, billingAddress }) => {
    const [shopNames, setShopNames] = useState<Record<string, string>>({});

    // Shop-Namen einmalig per API laden
    useEffect(() => {
        const uniqueShopIds = [...new Set(activeItems.map((i) => i.shopId))];
        void Promise.allSettled(
            uniqueShopIds.map((id) =>
                storefrontService.getShopById(id).then((shop) => ({ id, name: shop.name }))
            )
        ).then((results) => {
            const names: Record<string, string> = {};
            results.forEach((r) => {
                if (r.status === "fulfilled") names[r.value.id] = r.value.name;
            });
            setShopNames(names);
        });
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    // Items nach shopId gruppieren
    const groups = activeItems.reduce<Record<string, CartItem[]>>((acc, item) => {
        (acc[item.shopId] ??= []).push(item);
        return acc;
    }, {});

    const totalPrice = activeItems.reduce((sum, i) => sum + i.unitPrice * i.quantity, 0);

    const formatAddress = (a: Address) =>
        `${a.street} ${a.houseNumber}, ${a.postalCode} ${a.city}, ${a.country}`;

    return (
        <div className="order-summary">
            {/* Items gruppiert nach Shop */}
            {Object.entries(groups).map(([shopId, items]) => (
                <section key={shopId} className="order-summary__group">
                    <h3 className="order-summary__shop-heading">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" strokeWidth="2" aria-hidden="true">
                            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                            <polyline points="9 22 9 12 15 12 15 22" />
                        </svg>
                        {shopNames[shopId] ?? "Shop wird geladen…"}
                    </h3>

                    <ul className="order-summary__items">
                        {items.map((item) => {
                            const imageUrl = item.titleImage
                                ? storefrontService.getProductImageUrl(item.productId)
                                : null;
                            return (
                                <li key={item.productId} className="order-summary__item">
                                    <div className="order-summary__item-image">
                                        {imageUrl
                                            ? <img src={imageUrl} alt={item.productName} />
                                            : <div className="order-summary__item-image-placeholder" aria-hidden="true" />
                                        }
                                    </div>
                                    <div className="order-summary__item-details">
                                        <span className="order-summary__item-name">{item.productName}</span>
                                        <span className="order-summary__item-unit">
                                            {fmt(item.unitPrice)} × {item.quantity}
                                        </span>
                                    </div>
                                    <strong className="order-summary__item-total">
                                        {fmt(item.unitPrice * item.quantity)}
                                    </strong>
                                </li>
                            );
                        })}
                    </ul>
                </section>
            ))}

            {/* Gesamtpreis */}
            <div className="order-summary__total-row">
                <span>Gesamtpreis</span>
                <strong className="order-summary__total-price">{fmt(totalPrice)}</strong>
            </div>

            {/* Adressen */}
            <div className="order-summary__addresses">
                <div className="order-summary__address-block">
                    <h4>Lieferadresse</h4>
                    <p>{formatAddress(shippingAddress)}</p>
                </div>
                <div className="order-summary__address-block">
                    <h4>Rechnungsadresse</h4>
                    <p>{formatAddress(billingAddress)}</p>
                </div>
            </div>

            {/* MVP-Hinweis */}
            <p className="order-summary__payment-hint">
                Zahlungsmethode Vorkasse:
                Bitte überweisen Sie den Betrag an:
                IBAN: DE XX XXXX XXXX XXXX XX
                BIC : XXXXX
                Verwendungszweg: Order-Id
            </p>
        </div>
    );
};

export default OrderSummary;