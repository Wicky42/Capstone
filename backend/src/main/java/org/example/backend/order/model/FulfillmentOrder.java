package org.example.backend.order.model;

import lombok.*;
import org.example.backend.common.model.Address;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document(collection = "fulfillment_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentOrder {

    @Id
    private String id;

    @Field("order_number")
    private String orderNumber;

    @Field("customer_id")
    private String customerId;

    private List<OrderItem> items;

    @Field("seller_order_ids")
    private List<String> sellerOrderIds;

    @Field("shop_ids")
    private List<String> shopIds;

    @Field("total_price")
    private double totalPrice;

    @Field("shipping_address")
    private Address shippingAddress;

    @Field("billing_address")
    private Address billingAddress;

    @Field("invoice_id")
    private String invoiceId;

    @Field("is_paid")
    private boolean isPaid;

    private FulfillmentOrderStatus status;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

    @Field("completed_at")
    private Instant completedAt;
}
