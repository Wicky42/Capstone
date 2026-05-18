package org.example.backend.order.dto;

import java.util.List;

/**
 * Erweiterte Admin-Ansicht einer FulfillmentOrder inklusive aller zugehörigen SellerOrders.
 */
public record AdminFulfillmentOrderDetail(
        FulfillmentOrderResponse fulfillmentOrder,
        List<SellerOrderResponse> sellerOrders
) {
}

