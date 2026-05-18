import type { Address } from "./address";

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
    orderId: string;
    invoiceId: string;
    status: string;
    totalPrice: number;
};

