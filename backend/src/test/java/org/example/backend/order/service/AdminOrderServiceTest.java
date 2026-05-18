package org.example.backend.order.service;

import org.example.backend.common.exception.OrderNotFoundException;
import org.example.backend.order.dto.AdminFulfillmentOrderDetail;
import org.example.backend.order.dto.FulfillmentOrderResponse;
import org.example.backend.order.model.FulfillmentOrder;
import org.example.backend.order.model.FulfillmentOrderStatus;
import org.example.backend.order.model.SellerOrder;
import org.example.backend.order.model.SellerOrderStatus;
import org.example.backend.order.repository.FulfillmentOrderRepository;
import org.example.backend.order.repository.SellerOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    FulfillmentOrderRepository fulfillmentOrderRepository;

    @Mock
    SellerOrderRepository sellerOrderRepository;

    @InjectMocks
    AdminOrderService adminOrderService;

    private FulfillmentOrder processingOrder;
    private SellerOrder shippedSellerOrder;

    @BeforeEach
    void setUp() {
        processingOrder = FulfillmentOrder.builder()
                .id("fo-1")
                .orderNumber("ORD-2026-000001")
                .customerId("customer-1")
                .status(FulfillmentOrderStatus.PROCESSING)
                .build();

        shippedSellerOrder = SellerOrder.builder()
                .id("so-1")
                .orderNumber("SO-2026-000001")
                .fulfillmentOrderId("fo-1")
                .sellerId("seller-1")
                .status(SellerOrderStatus.SHIPPED_TO_WAREHOUSE)
                .build();
    }

    // ═══════════════════ getAllFulfillmentOrders ══════════════════════════════

    @Test
    void getAllFulfillmentOrders_returnsMappedList() {
        when(fulfillmentOrderRepository.findAll()).thenReturn(List.of(processingOrder));

        List<FulfillmentOrderResponse> result = adminOrderService.getAllFulfillmentOrders();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().orderNumber()).isEqualTo("ORD-2026-000001");
        assertThat(result.getFirst().status()).isEqualTo(FulfillmentOrderStatus.PROCESSING);
    }

    @Test
    void getAllFulfillmentOrders_returnsEmptyList_whenNoOrders() {
        when(fulfillmentOrderRepository.findAll()).thenReturn(List.of());

        assertThat(adminOrderService.getAllFulfillmentOrders()).isEmpty();
    }

    // ═══════════════════ getFulfillmentOrderDetail ════════════════════════════

    @Test
    void getFulfillmentOrderDetail_returnsDetailWithSellerOrders() {
        when(fulfillmentOrderRepository.findById("fo-1")).thenReturn(Optional.of(processingOrder));
        when(sellerOrderRepository.findByFulfillmentOrderId("fo-1")).thenReturn(List.of(shippedSellerOrder));

        AdminFulfillmentOrderDetail result = adminOrderService.getFulfillmentOrderDetail("fo-1");

        assertThat(result.fulfillmentOrder().orderNumber()).isEqualTo("ORD-2026-000001");
        assertThat(result.sellerOrders()).hasSize(1);
        assertThat(result.sellerOrders().getFirst().orderNumber()).isEqualTo("SO-2026-000001");
    }

    @Test
    void getFulfillmentOrderDetail_throwsOrderNotFoundException_whenOrderDoesNotExist() {
        when(fulfillmentOrderRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminOrderService.getFulfillmentOrderDetail("unknown"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    // ═══════════════════ updateFulfillmentOrderStatus ════════════════════════

    @Test
    void updateFulfillmentOrderStatus_PROCESSING_to_READY_FOR_FINAL_SHIPMENT_succeeds_whenAllSellerOrdersReady() {
        when(fulfillmentOrderRepository.findById("fo-1")).thenReturn(Optional.of(processingOrder));
        when(sellerOrderRepository.findByFulfillmentOrderId("fo-1")).thenReturn(List.of(shippedSellerOrder));
        when(fulfillmentOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FulfillmentOrderResponse result = adminOrderService.updateFulfillmentOrderStatus(
                "fo-1", FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT);

        assertThat(result.status()).isEqualTo(FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT);
        assertThat(result.updatedAt()).isNotNull();
    }

    @Test
    void updateFulfillmentOrderStatus_READY_FOR_FINAL_SHIPMENT_to_COMPLETED_succeeds() {
        processingOrder.setStatus(FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT);

        when(fulfillmentOrderRepository.findById("fo-1")).thenReturn(Optional.of(processingOrder));
        when(fulfillmentOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FulfillmentOrderResponse result = adminOrderService.updateFulfillmentOrderStatus(
                "fo-1", FulfillmentOrderStatus.COMPLETED);

        assertThat(result.status()).isEqualTo(FulfillmentOrderStatus.COMPLETED);
        assertThat(result.completedAt()).isNotNull();
    }

    @Test
    void updateFulfillmentOrderStatus_throwsIllegalArgumentException_whenTransitionIsInvalid() {
        // PROCESSING → COMPLETED (Schritt übersprungen)
        when(fulfillmentOrderRepository.findById("fo-1")).thenReturn(Optional.of(processingOrder));

        assertThatThrownBy(() -> adminOrderService.updateFulfillmentOrderStatus(
                "fo-1", FulfillmentOrderStatus.COMPLETED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ungültiger Statusübergang");

        verify(fulfillmentOrderRepository, never()).save(any());
    }

    @Test
    void updateFulfillmentOrderStatus_throwsIllegalArgumentException_whenStatusGoesBackwards() {
        // READY_FOR_FINAL_SHIPMENT → PROCESSING
        processingOrder.setStatus(FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT);
        when(fulfillmentOrderRepository.findById("fo-1")).thenReturn(Optional.of(processingOrder));

        assertThatThrownBy(() -> adminOrderService.updateFulfillmentOrderStatus(
                "fo-1", FulfillmentOrderStatus.PROCESSING))
                .isInstanceOf(IllegalArgumentException.class);

        verify(fulfillmentOrderRepository, never()).save(any());
    }

    @Test
    void updateFulfillmentOrderStatus_throwsIllegalStateException_whenNotAllSellerOrdersReady() {
        SellerOrder notReadyOrder = SellerOrder.builder()
                .id("so-2")
                .fulfillmentOrderId("fo-1")
                .status(SellerOrderStatus.IN_PREPARATION) // noch nicht bereit
                .build();

        when(fulfillmentOrderRepository.findById("fo-1")).thenReturn(Optional.of(processingOrder));
        when(sellerOrderRepository.findByFulfillmentOrderId("fo-1")).thenReturn(List.of(notReadyOrder));

        assertThatThrownBy(() -> adminOrderService.updateFulfillmentOrderStatus(
                "fo-1", FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SHIPPED_TO_WAREHOUSE");

        verify(fulfillmentOrderRepository, never()).save(any());
    }

    @Test
    void updateFulfillmentOrderStatus_throwsIllegalStateException_whenOneOfMultipleSellerOrdersNotReady() {
        SellerOrder readyOrder = SellerOrder.builder()
                .id("so-1").fulfillmentOrderId("fo-1").status(SellerOrderStatus.SHIPPED_TO_WAREHOUSE).build();
        SellerOrder notReadyOrder = SellerOrder.builder()
                .id("so-2").fulfillmentOrderId("fo-1").status(SellerOrderStatus.CONFIRMED).build();

        when(fulfillmentOrderRepository.findById("fo-1")).thenReturn(Optional.of(processingOrder));
        when(sellerOrderRepository.findByFulfillmentOrderId("fo-1")).thenReturn(List.of(readyOrder, notReadyOrder));

        assertThatThrownBy(() -> adminOrderService.updateFulfillmentOrderStatus(
                "fo-1", FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT))
                .isInstanceOf(IllegalStateException.class);

        verify(fulfillmentOrderRepository, never()).save(any());
    }

    @Test
    void updateFulfillmentOrderStatus_throwsIllegalStateException_whenNoSellerOrdersExist() {
        when(fulfillmentOrderRepository.findById("fo-1")).thenReturn(Optional.of(processingOrder));
        when(sellerOrderRepository.findByFulfillmentOrderId("fo-1")).thenReturn(List.of());

        assertThatThrownBy(() -> adminOrderService.updateFulfillmentOrderStatus(
                "fo-1", FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Keine SellerOrders");

        verify(fulfillmentOrderRepository, never()).save(any());
    }

    @Test
    void updateFulfillmentOrderStatus_throwsOrderNotFoundException_whenOrderDoesNotExist() {
        when(fulfillmentOrderRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminOrderService.updateFulfillmentOrderStatus(
                "unknown", FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void updateFulfillmentOrderStatus_COMPLETED_sellerOrderStatusCompleted_isAlsoAccepted() {
        // COMPLETED-Status bei SellerOrder gilt ebenfalls als "bereit"
        SellerOrder completedSellerOrder = SellerOrder.builder()
                .id("so-1").fulfillmentOrderId("fo-1").status(SellerOrderStatus.COMPLETED).build();

        when(fulfillmentOrderRepository.findById("fo-1")).thenReturn(Optional.of(processingOrder));
        when(sellerOrderRepository.findByFulfillmentOrderId("fo-1")).thenReturn(List.of(completedSellerOrder));
        when(fulfillmentOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FulfillmentOrderResponse result = adminOrderService.updateFulfillmentOrderStatus(
                "fo-1", FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT);

        assertThat(result.status()).isEqualTo(FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT);
    }
}

