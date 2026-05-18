package org.example.backend.order.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.OrderNotFoundException;
import org.example.backend.order.dto.AdminFulfillmentOrderDetail;
import org.example.backend.order.dto.FulfillmentOrderResponse;
import org.example.backend.order.dto.SellerOrderResponse;
import org.example.backend.order.model.FulfillmentOrder;
import org.example.backend.order.model.FulfillmentOrderStatus;
import org.example.backend.order.model.SellerOrder;
import org.example.backend.order.model.SellerOrderStatus;
import org.example.backend.order.model.SellerOrderStatus;
import org.example.backend.order.repository.FulfillmentOrderRepository;
import org.example.backend.order.repository.SellerOrderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final FulfillmentOrderRepository fulfillmentOrderRepository;
    private final SellerOrderRepository sellerOrderRepository;

    /** Erlaubte Statusübergänge für Admin */
    private static final Map<FulfillmentOrderStatus, FulfillmentOrderStatus> ALLOWED_TRANSITIONS = Map.of(
            FulfillmentOrderStatus.PROCESSING,                FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT,
            FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT,  FulfillmentOrderStatus.COMPLETED
    );

    /** SellerOrderStatus-Werte, die als "bereit für finalen Versand" gelten */
    private static final Set<SellerOrderStatus> READY_STATUSES = Set.of(
            SellerOrderStatus.SHIPPED_TO_WAREHOUSE,
            SellerOrderStatus.COMPLETED
    );

    public List<FulfillmentOrderResponse> getAllFulfillmentOrders() {
        return fulfillmentOrderRepository.findAll()
                .stream()
                .map(FulfillmentOrderResponse::from)
                .toList();
    }

    public AdminFulfillmentOrderDetail getFulfillmentOrderDetail(String id) {
        FulfillmentOrder order = findOrderById(id);
        List<SellerOrderResponse> sellerOrders = sellerOrderRepository
                .findByFulfillmentOrderId(order.getId())
                .stream()
                .map(SellerOrderResponse::from)
                .toList();
        return new AdminFulfillmentOrderDetail(FulfillmentOrderResponse.from(order), sellerOrders);
    }

    public FulfillmentOrderResponse updateFulfillmentOrderStatus(String id, FulfillmentOrderStatus newStatus) {
        FulfillmentOrder order = findOrderById(id);
        FulfillmentOrderStatus currentStatus = order.getStatus();
        FulfillmentOrderStatus expectedNext = ALLOWED_TRANSITIONS.get(currentStatus);

        if (expectedNext == null || !expectedNext.equals(newStatus)) {
            throw new IllegalArgumentException(
                    "Ungültiger Statusübergang von " + currentStatus + " nach " + newStatus);
        }

        if (newStatus == FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT) {
            validateAllSellerOrdersReady(order);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());
        if (newStatus == FulfillmentOrderStatus.COMPLETED) {
            order.setCompletedAt(Instant.now());
        }

        return FulfillmentOrderResponse.from(fulfillmentOrderRepository.save(order));
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private FulfillmentOrder findOrderById(String id) {
        return fulfillmentOrderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException("FulfillmentOrder mit ID " + id + " nicht gefunden"));
    }

    private void validateAllSellerOrdersReady(FulfillmentOrder order) {
        List<SellerOrderStatus> sellerStatuses = sellerOrderRepository
                .findByFulfillmentOrderId(order.getId())
                .stream()
                .map(SellerOrder::getStatus)
                .toList();

        if (sellerStatuses.isEmpty()) {
            throw new IllegalStateException("Keine SellerOrders für diese Bestellung gefunden");
        }

        if (!READY_STATUSES.containsAll(sellerStatuses)) {
            throw new IllegalStateException(
                    "Nicht alle SellerOrders haben den Status SHIPPED_TO_WAREHOUSE oder höher");
        }
    }
}



