package org.example.backend.order.dto;

import org.example.backend.order.model.FulfillmentOrderStatus;

import java.math.BigDecimal;

public record CheckoutResponse(
        String orderNumber,
        String invoiceNumber,
        FulfillmentOrderStatus status,
        BigDecimal totalPrice
) {
}
