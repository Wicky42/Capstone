package org.example.backend.order.model;

import lombok.*;
import org.example.backend.common.model.Address;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document(collection = "seller_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrder {
    @Id
    private String id;

    @Field("order_number")
    private String orderNumber;

    @Field("fulfillment_order_id")
    private String fulfillmentOrderId;

    @Field("seller_id")
    private String sellerId;

    @Field("shop_id")
    private String shopId;

    private List<OrderItem> items;

    @Field("warehouse_address")
    private Address warehouseAddress;

    private SellerOrderStatus status;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

    @Field("completed_at")
    private Instant completedAt;

}
