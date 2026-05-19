import { type FC, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { orderService } from '../../services/orderService';
import type { FulfillmentOrder } from '../../types/order';
import OrderStatusBadge from '../../components/order/OrderStatusBadge';
import './AccountOrdersPage.css';

const fmt = (n: number) => n.toLocaleString('de-DE', { style: 'currency', currency: 'EUR' });

const AccountOrdersPage: FC = () => {
    const navigate = useNavigate();
    const [orders, setOrders] = useState<FulfillmentOrder[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        orderService
            .getOrders()
            .then((data) =>
                setOrders([...data].sort((a, b) =>
                    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
                ))
            )
            .catch((err) => {
                if (err?.response?.status === 401 || err?.response?.status === 403) {
                    navigate('/register', { replace: true });
                } else {
                    setError('Deine Bestellungen konnten nicht geladen werden.');
                }
            })
            .finally(() => setLoading(false));
    }, [navigate]);

    if (loading) {
        return (
            <div className="page-centered">
                <p>Bestellungen werden geladen…</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="page-centered">
                <div className="card account-orders-page__error">
                    <p>{error}</p>
                    <Link to="/" className="button-primary">Zum Marktplatz</Link>
                </div>
            </div>
        );
    }

    return (
        <div className="account-orders-page">
            <div className="container account-orders-page__container">
                <h1 className="account-orders-page__title">Meine Bestellungen</h1>

                {orders.length === 0 ? (
                    <div className="card account-orders-page__empty">
                        <p>Du hast noch keine Bestellungen aufgegeben.</p>
                        <Link to="/" className="button-primary">Jetzt einkaufen</Link>
                    </div>
                ) : (
                    <ul className="account-orders-page__list">
                        {orders.map((order) => (
                            <li key={order.orderNumber} className="card account-orders-page__item">
                                <div className="account-orders-page__item-info">
                                    <span className="account-orders-page__order-number">
                                        {order.orderNumber}
                                    </span>
                                    <span className="account-orders-page__date">
                                        {new Date(order.createdAt).toLocaleDateString('de-DE', {
                                            day: '2-digit',
                                            month: 'long',
                                            year: 'numeric',
                                        })}
                                    </span>
                                </div>
                                <OrderStatusBadge status={order.status} />
                                <strong className="account-orders-page__total">
                                    {fmt(order.totalPrice)}
                                </strong>
                                <Link
                                    to={`/account/orders/${order.orderNumber}`}
                                    className="button-secondary account-orders-page__detail-link"
                                >
                                    Details
                                </Link>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
};

export default AccountOrdersPage;