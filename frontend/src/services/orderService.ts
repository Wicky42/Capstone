import api from "./api";
import type { CheckoutRequest, CheckoutResponse, FulfillmentOrder } from "../types/order";

export const orderService = {
    async checkout(request: CheckoutRequest): Promise<CheckoutResponse> {
        const response = await api.post<CheckoutResponse>("/orders/checkout", request);
        return response.data;
    },

    async getOrderById(orderId: string): Promise<FulfillmentOrder> {
        const response = await api.get<FulfillmentOrder>(`/customer/orders/${orderId}`);
        return response.data;
    },
};
