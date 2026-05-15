package org.example.backend.order.model;

import lombok.*;
import org.example.backend.common.model.Address;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "customer_invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInvoice {
    @Id
    private String id;

    @Field("fulfillment_order_id")
    private String fulfillmentOrderId;

    @Field("customer_id")
    private String customerId;

    private List<OrderItem> items;

    @Field("total_amount")
    private BigDecimal totalAmount;

    @Field("billing_address")
    private Address billingAddress;

    @Field("invoice_number")
    private String invoiceNumber;

    @Field("created_at")
    private Instant createdAt;
}
