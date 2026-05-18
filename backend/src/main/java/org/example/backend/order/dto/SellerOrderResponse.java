package org.example.backend.order.dto;

import org.example.backend.common.model.Address;
import org.example.backend.order.model.OrderItem;
import org.example.backend.order.model.SellerOrder;
import org.example.backend.order.model.SellerOrderStatus;

import java.time.Instant;
import java.util.List;

public record SellerOrderResponse(
        String id,
        String fulfillmentOrderId,
        String shopId,
        List<OrderItem> items,
        Address warehouseAddress,
        SellerOrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static SellerOrderResponse from(SellerOrder order) {
        return new SellerOrderResponse(
                order.getId(),
                order.getFulfillmentOrderId(),
                order.getShopId(),
                order.getItems(),
                order.getWarehouseAddress(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}

