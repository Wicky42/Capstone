package org.example.backend.order.dto;

import org.example.backend.order.model.FulfillmentOrderStatus;

public record CheckoutResponse(
        String orderNumber,
        String invoiceNumber,
        FulfillmentOrderStatus status,
        double totalPrice
) {
}
