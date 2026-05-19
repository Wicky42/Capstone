import { type FC, useEffect, useState } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { orderService } from '../../services/orderService';
import { storefrontService } from '../../services/storefrontService';
import type { FulfillmentOrder } from '../../types/order';
import OrderStatusBadge from '../../components/order/OrderStatusBadge';
import './AccountOrderDetailPage.css';

const fmt = (n: number) => n.toLocaleString('de-DE', { style: 'currency', currency: 'EUR' });

const AccountOrderDetailPage: FC = () => {
    const { orderId } = useParams<{ orderId: string }>();
    const navigate = useNavigate();
    const [order, setOrder] = useState<FulfillmentOrder | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!orderId) {
            navigate('/account/orders', { replace: true });
            return;
        }
        orderService
            .getOrderById(orderId)
            .then(setOrder)
            .catch((err) => {
                if (err?.response?.status === 401 || err?.response?.status === 403) {
                    navigate('/register', { replace: true });
                } else {
                    setError('Bestellung nicht gefunden oder kein Zugriff.');
                }
            })
            .finally(() => setLoading(false));
    }, [orderId, navigate]);

    if (loading) {
        return (
            <div className="page-centered">
                <p>Bestellung wird geladen…</p>
            </div>
        );
    }

    if (error || !order) {
        return (
            <div className="page-centered">
                <div className="card order-detail-page__error">
                    <h1>Bestellung nicht gefunden</h1>
                    <p>{error ?? 'Diese Bestellung existiert nicht.'}</p>
                    <Link to="/account/orders" className="button-primary">Zu meinen Bestellungen</Link>
                </div>
            </div>
        );
    }

    const formatAddress = (a: typeof order.shippingAddress) =>
        `${a.street} ${a.houseNumber}, ${a.postalCode} ${a.city}, ${a.country}`;

    return (
        <div className="order-detail-page">
            <div className="container order-detail-page__container">

                <div className="order-detail-page__back">
                    <Link to="/account/orders" className="order-detail-page__back-link">
                        ← Alle Bestellungen
                    </Link>
                </div>

                {/* Header */}
                <div className="card order-detail-page__header">
                    <div className="order-detail-page__header-main">
                        <h1 className="order-detail-page__order-number">{order.orderNumber}</h1>
                        <OrderStatusBadge status={order.status} />
                    </div>
                    <dl className="order-detail-page__meta">
                        <div>
                            <dt>Bestelldatum</dt>
                            <dd>
                                {new Date(order.createdAt).toLocaleDateString('de-DE', {
                                    day: '2-digit',
                                    month: 'long',
                                    year: 'numeric',
                                })}
                            </dd>
                        </div>
                        <div>
                            <dt>Gesamtbetrag</dt>
                            <dd><strong>{fmt(order.totalPrice)}</strong></dd>
                        </div>
                        <div>
                            <dt>Zahlung</dt>
                            <dd>{order.isPaid ? 'Bezahlt' : 'Ausstehend'}</dd>
                        </div>
                        {order.invoiceId && (
                            <div>
                                <dt>Rechnung</dt>
                                <dd>
                                    <Link to={`/account/invoices/${order.invoiceId}`} className="order-detail-page__invoice-link">
                                        Rechnung ansehen
                                    </Link>
                                </dd>
                            </div>
                        )}
                    </dl>
                </div>

                {/* Items */}
                <section className="order-detail-page__section">
                    <h2 className="order-detail-page__section-title">Bestellte Produkte</h2>
                    <ul className="order-detail-page__items">
                        {order.items.map((item, i) => {
                            const imageUrl = item.titleImage
                                ? storefrontService.getProductImageUrl(item.productId)
                                : null;
                            return (
                                <li key={`${item.productId}-${i}`} className="order-detail-page__item">
                                    <div className="order-detail-page__item-image">
                                        {imageUrl
                                            ? <img src={imageUrl} alt={item.productName} />
                                            : <div className="order-detail-page__item-placeholder" aria-hidden />
                                        }
                                    </div>
                                    <div className="order-detail-page__item-details">
                                        <span className="order-detail-page__item-name">{item.productName}</span>
                                        <span className="order-detail-page__item-unit">
                                            {fmt(item.unitPrice)} × {item.quantity}
                                        </span>
                                    </div>
                                    <strong className="order-detail-page__item-total">
                                        {fmt(item.unitPrice * item.quantity)}
                                    </strong>
                                </li>
                            );
                        })}
                    </ul>
                    <div className="order-detail-page__total-row">
                        <span>Gesamtpreis</span>
                        <strong>{fmt(order.totalPrice)}</strong>
                    </div>
                </section>

                {/* Addresses */}
                <div className="order-detail-page__addresses">
                    <div className="card">
                        <h3 className="order-detail-page__address-title">Lieferadresse</h3>
                        <p>{formatAddress(order.shippingAddress)}</p>
                    </div>
                    <div className="card">
                        <h3 className="order-detail-page__address-title">Rechnungsadresse</h3>
                        <p>{formatAddress(order.billingAddress)}</p>
                    </div>
                </div>

            </div>
        </div>
    );
};

export default AccountOrderDetailPage;