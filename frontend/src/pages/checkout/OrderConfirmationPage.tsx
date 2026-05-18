import { type FC, useEffect, useState } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import { orderService } from "../../services/orderService";
import { storefrontService } from "../../services/storefrontService";
import type { FulfillmentOrder } from "../../types/order";
import "./OrderConfirmationPage.css";

const fmt = (n: number) => n.toLocaleString("de-DE", { style: "currency", currency: "EUR" });

const OrderConfirmationPage: FC = () => {
    const { orderId } = useParams<{ orderId: string }>();
    const navigate = useNavigate();
    const [order, setOrder] = useState<FulfillmentOrder | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!orderId) {
            navigate("/", { replace: true });
            return;
        }
        orderService
            .getOrderById(orderId)
            .then(setOrder)
            .catch(() => setError("Bestellung nicht gefunden oder kein Zugriff."))
            .finally(() => setLoading(false));
    }, [orderId, navigate]);

    if (loading) {
        return (
            <div className="page page-centered">
                <p>Bestellung wird geladen…</p>
            </div>
        );
    }

    if (error || !order) {
        return (
            <div className="page page-centered">
                <div className="confirmation-page__error card">
                    <h1>Bestellung nicht gefunden</h1>
                    <p>{error ?? "Diese Bestellung existiert nicht."}</p>
                    <Link to="/" className="button-primary">Zum Marktplatz</Link>
                </div>
            </div>
        );
    }

    const formatAddress = (a: typeof order.shippingAddress) =>
        `${a.street} ${a.houseNumber}, ${a.postalCode} ${a.city}, ${a.country}`;

    const createdDate = new Date(order.createdAt).toLocaleDateString("de-DE", {
        day: "2-digit",
        month: "long",
        year: "numeric",
    });

    return (
        <div className="page confirmation-page">
            <div className="container confirmation-page__container">

                {/* Erfolgs-Banner */}
                <div className="confirmation-page__banner">
                    <div className="confirmation-page__banner-icon" aria-hidden="true">✓</div>
                    <div>
                        <h1 className="confirmation-page__banner-title">
                            Deine Bestellung wurde erfolgreich aufgegeben!
                        </h1>
                        <p className="confirmation-page__banner-sub">
                            Vielen Dank für deinen Einkauf. Du findest deine Bestellungen jederzeit
                            in deinem Kundenkonto.
                        </p>
                    </div>
                </div>

                {/* Meta-Infos */}
                <div className="confirmation-page__meta card">
                    <dl className="confirmation-page__meta-list">
                        <div>
                            <dt>Bestellnummer</dt>
                            <dd><strong>{order.orderNumber}</strong></dd>
                        </div>
                        <div>
                            <dt>Rechnungsnummer</dt>
                            <dd>{order.invoiceId}</dd>
                        </div>
                        <div>
                            <dt>Bestelldatum</dt>
                            <dd>{createdDate}</dd>
                        </div>
                        <div>
                            <dt>Gesamtbetrag</dt>
                            <dd><strong>{fmt(order.totalPrice)}</strong></dd>
                        </div>
                        <div>
                            <dt>Status</dt>
                            <dd>
                                <span className="confirmation-page__status-badge">{order.status}</span>
                            </dd>
                        </div>
                    </dl>
                </div>

                {/* Bestellte Produkte */}
                <section className="confirmation-page__section">
                    <h2 className="confirmation-page__section-title">Bestellte Produkte</h2>
                    <ul className="confirmation-page__items">
                        {order.items.map((item) => {
                            const imageUrl = item.titleImage
                                ? storefrontService.getProductImageUrl(item.productId)
                                : null;
                            return (
                                <li key={item.productId} className="confirmation-page__item">
                                    <div className="confirmation-page__item-image">
                                        {imageUrl
                                            ? <img src={imageUrl} alt={item.productName} />
                                            : <div className="confirmation-page__item-placeholder" aria-hidden="true" />
                                        }
                                    </div>
                                    <div className="confirmation-page__item-details">
                                        <span className="confirmation-page__item-name">{item.productName}</span>
                                        <span className="confirmation-page__item-unit">
                                            {fmt(item.unitPrice)} × {item.quantity}
                                        </span>
                                    </div>
                                    <strong className="confirmation-page__item-total">
                                        {fmt(item.unitPrice * item.quantity)}
                                    </strong>
                                </li>
                            );
                        })}
                    </ul>
                    <div className="confirmation-page__total-row">
                        <span>Gesamtpreis</span>
                        <strong>{fmt(order.totalPrice)}</strong>
                    </div>
                </section>

                {/* Adressen */}
                <div className="confirmation-page__addresses">
                    <div className="card">
                        <h3 className="confirmation-page__address-title">Lieferadresse</h3>
                        <p>{formatAddress(order.shippingAddress)}</p>
                    </div>
                    <div className="card">
                        <h3 className="confirmation-page__address-title">Rechnungsadresse</h3>
                        <p>{formatAddress(order.billingAddress)}</p>
                    </div>
                </div>

                {/* Hinweis */}
                <p className="confirmation-page__account-hint">
                    Eine Bestellbestätigung findest du in deinem Kundenkonto unter „Meine Bestellungen".
                </p>

                {/* Links */}
                <div className="confirmation-page__actions">
                    <Link to="/account/orders" className="button-primary">
                        Zu meinen Bestellungen
                    </Link>
                    <Link to="/" className="button-secondary">
                        ← Weiter einkaufen
                    </Link>
                </div>

            </div>
        </div>
    );
};

export default OrderConfirmationPage;

