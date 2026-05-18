package org.example.backend.order.dto;

import org.example.backend.common.model.Address;
import org.example.backend.order.model.CustomerInvoice;
import org.example.backend.order.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CustomerInvoiceResponse(
        String fulfillmentOrderId,
        String customerId,
        List<OrderItem> items,
        BigDecimal totalAmount,
        Address billingAddress,
        String invoiceNumber,
        Instant createdAt
) {
    public static CustomerInvoiceResponse from(CustomerInvoice invoice) {
        return new CustomerInvoiceResponse(
                invoice.getFulfillmentOrderId(),
                invoice.getCustomerId(),
                invoice.getItems(),
                invoice.getTotalAmount(),
                invoice.getBillingAddress(),
                invoice.getInvoiceNumber(),
                invoice.getCreatedAt()
        );
    }
}
