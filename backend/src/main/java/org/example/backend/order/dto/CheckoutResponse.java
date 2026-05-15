package org.example.backend.order.dto;

import org.example.backend.order.model.FulfillmentOrderStatus;

//{
//        "orderId": "fo-1001",
//        "invoiceId": "inv-3001",
//        "status": "CREATED",
//        "totalPrice": 23.47
//        }
public record CheckoutResponse(
        String orderId,
        String invoiceId,
        FulfillmentOrderStatus status,
        double totalPrice
) {
}
