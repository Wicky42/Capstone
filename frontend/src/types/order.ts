import type { Address } from "./address";

export type FulfillmentOrderStatus =
    | 'CREATED'
    | 'PROCESSING'
    | 'READY_FOR_FINAL_SHIPMENT'
    | 'COMPLETED'
    | 'CANCELLED';

export type OrderItemRequest = {
    productId: string;
    productName: string;
    unitPrice: number;
    quantity: number;
    titleImage: string | null;
    shopId: string;
    sellerId: string;
};

export type CheckoutRequest = {
    items: OrderItemRequest[];
    shippingAddress: Address;
    billingAddress: Address;
};

export type CheckoutResponse = {
    orderNumber: string;
    invoiceId: string;
    status: string;
    totalPrice: number;
};

export type OrderItem = {
    productId: string;
    productName: string;
    unitPrice: number;
    quantity: number;
    titleImage: string | null;
    shopId: string;
    sellerId: string;
};

export type FulfillmentOrder = {
    orderNumber: string;
    customerId: string;
    items: OrderItem[];
    sellerOrderIds: string[];
    totalPrice: number;
    shippingAddress: Address;
    billingAddress: Address;
    invoiceId: string;
    isPaid: boolean;
    status: FulfillmentOrderStatus;
    createdAt: string;
};
