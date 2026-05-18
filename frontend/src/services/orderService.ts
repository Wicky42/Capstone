import api from "./api";
import type { CheckoutRequest, CheckoutResponse } from "../types/order";
export const orderService = {
    async checkout(request: CheckoutRequest): Promise<CheckoutResponse> {
        const response = await api.post<CheckoutResponse>("/orders/checkout", request);
        return response.data;
    },
};
