package org.example.backend.order.dto;

import org.example.backend.common.model.Address;
import org.example.backend.order.model.FulfillmentOrder;
import org.example.backend.order.model.FulfillmentOrderStatus;
import org.example.backend.order.model.OrderItem;
import java.time.Instant;
import java.util.List;

public record FulfillmentOrderResponse(
        String orderNumber,
        String customerId,
        List<OrderItem> items,
        List<String> sellerOrderIds,
        List<String> shopIds,
        double totalPrice,
        Address shippingAddress,
        Address billingAddress,
        String invoiceId,
        boolean isPaid,
        FulfillmentOrderStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt

) {
    public static FulfillmentOrderResponse from(FulfillmentOrder order) {
        return new FulfillmentOrderResponse(
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getItems(),
                order.getSellerOrderIds(),
                order.getShopIds(),
                order.getTotalPrice(),
                order.getShippingAddress(),
                order.getBillingAddress(),
                order.getInvoiceId(),
                order.isPaid(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCompletedAt()
        );
    }
}
