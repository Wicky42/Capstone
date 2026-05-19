import type { FulfillmentOrderStatus } from '../../types/order';
import './OrderStatusBadge.css';

const STATUS_LABEL: Record<FulfillmentOrderStatus, string> = {
    CREATED: 'Erstellt',
    PROCESSING: 'In Bearbeitung',
    READY_FOR_FINAL_SHIPMENT: 'Versandbereit',
    COMPLETED: 'Abgeschlossen',
    CANCELLED: 'Storniert',
};

type Props = { readonly status: FulfillmentOrderStatus };

export default function OrderStatusBadge({ status }: Props) {
    const modifier = status.toLowerCase().replace(/_/g, '-');
    return (
        <span className={`osb osb--${modifier}`}>
            <span className="osb__dot" aria-hidden />
            {STATUS_LABEL[status]}
        </span>
    );
}