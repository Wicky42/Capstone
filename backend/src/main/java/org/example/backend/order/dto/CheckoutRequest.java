package org.example.backend.order.dto;

import org.example.backend.common.model.Address;
import org.example.backend.order.model.OrderItem;

public record CheckoutRequest(
        OrderItem[] items,
        Address shippingAddress,
        Address billingAddress

) {
}
