package org.example.backend.order.service;

import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.common.exception.OrderNotFoundException;
import org.example.backend.order.dto.SellerOrderResponse;
import org.example.backend.order.model.SellerOrder;
import org.example.backend.order.model.SellerOrderStatus;
import org.example.backend.order.repository.SellerOrderRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.service.UserService;
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
class SellerOrderServiceTest {

    @Mock
    UserService userService;

    @Mock
    SellerOrderRepository sellerOrderRepository;

    @InjectMocks
    SellerOrderService sellerOrderService;

    private Seller currentSeller;
    private SellerOrder ownOrder;

    @BeforeEach
    void setUp() {
        currentSeller = Seller.builder().id("seller-1").build();

        ownOrder = SellerOrder.builder()
                .id("order-1")
                .orderNumber("SO-2026-000001")
                .sellerId("seller-1")
                .status(SellerOrderStatus.CREATED)
                .build();
    }

    // ═══════════════════ getAllOrdersForCurrentSeller ═════════════════════════

    @Test
    void getAllOrdersForCurrentSeller_returnsListOfOrders() {
        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.getSellerOrderBySellerId("seller-1")).thenReturn(List.of(ownOrder));

        List<SellerOrderResponse> result = sellerOrderService.getAllOrdersForCurrentSeller();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().orderNumber()).isEqualTo("SO-2026-000001");
        assertThat(result.getFirst().status()).isEqualTo(SellerOrderStatus.CREATED);
        verify(userService).getCurrentSeller();
        verify(sellerOrderRepository).getSellerOrderBySellerId("seller-1");
    }

    @Test
    void getAllOrdersForCurrentSeller_returnsEmptyList_whenSellerHasNoOrders() {
        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.getSellerOrderBySellerId("seller-1")).thenReturn(List.of());

        List<SellerOrderResponse> result = sellerOrderService.getAllOrdersForCurrentSeller();

        assertThat(result).isEmpty();
    }

    // ═══════════════════ getSellerOrderById ══════════════════════════════════

    @Test
    void getSellerOrderById_returnsOrder_whenOrderBelongsToCurrentSeller() {
        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("order-1")).thenReturn(Optional.of(ownOrder));

        SellerOrderResponse result = sellerOrderService.getSellerOrderById("order-1");

        assertThat(result.orderNumber()).isEqualTo("SO-2026-000001");
        assertThat(result.status()).isEqualTo(SellerOrderStatus.CREATED);
    }

    @Test
    void getSellerOrderById_throwsOrderNotFoundException_whenOrderDoesNotExist() {
        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerOrderService.getSellerOrderById("unknown-id"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("unknown-id");
    }

    @Test
    void getSellerOrderById_throwsForbiddenAccessException_whenOrderBelongsToOtherSeller() {
        SellerOrder otherOrder = SellerOrder.builder()
                .id("order-2")
                .sellerId("seller-99")
                .status(SellerOrderStatus.CREATED)
                .build();

        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("order-2")).thenReturn(Optional.of(otherOrder));

        assertThatThrownBy(() -> sellerOrderService.getSellerOrderById("order-2"))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    // ═══════════════════ updateSellerOrderStatus ═════════════════════════════

    @Test
    void updateSellerOrderStatus_CREATED_to_CONFIRMED_succeeds() {
        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("order-1")).thenReturn(Optional.of(ownOrder));
        when(sellerOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SellerOrderResponse result = sellerOrderService.updateSellerOrderStatus("order-1", SellerOrderStatus.CONFIRMED);

        assertThat(result.status()).isEqualTo(SellerOrderStatus.CONFIRMED);
        assertThat(result.updatedAt()).isNotNull();
        verify(sellerOrderRepository).save(ownOrder);
    }

    @Test
    void updateSellerOrderStatus_CONFIRMED_to_IN_PREPARATION_succeeds() {
        ownOrder.setStatus(SellerOrderStatus.CONFIRMED);

        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("order-1")).thenReturn(Optional.of(ownOrder));
        when(sellerOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SellerOrderResponse result = sellerOrderService.updateSellerOrderStatus("order-1", SellerOrderStatus.IN_PREPARATION);

        assertThat(result.status()).isEqualTo(SellerOrderStatus.IN_PREPARATION);
    }

    @Test
    void updateSellerOrderStatus_IN_PREPARATION_to_SHIPPED_TO_WAREHOUSE_succeeds() {
        ownOrder.setStatus(SellerOrderStatus.IN_PREPARATION);

        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("order-1")).thenReturn(Optional.of(ownOrder));
        when(sellerOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SellerOrderResponse result = sellerOrderService.updateSellerOrderStatus("order-1", SellerOrderStatus.SHIPPED_TO_WAREHOUSE);

        assertThat(result.status()).isEqualTo(SellerOrderStatus.SHIPPED_TO_WAREHOUSE);
    }

    @Test
    void updateSellerOrderStatus_throwsIllegalArgumentException_whenTransitionIsInvalid() {
        // CREATED → IN_PREPARATION (Schritt übersprungen) ist ungültig
        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("order-1")).thenReturn(Optional.of(ownOrder));

        assertThatThrownBy(() -> sellerOrderService.updateSellerOrderStatus("order-1", SellerOrderStatus.IN_PREPARATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ungültiger Statusübergang");

        verify(sellerOrderRepository, never()).save(any());
    }

    @Test
    void updateSellerOrderStatus_throwsIllegalArgumentException_whenStatusGoesBackwards() {
        // CONFIRMED → CREATED (Rückschritt) ist ungültig
        ownOrder.setStatus(SellerOrderStatus.CONFIRMED);

        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("order-1")).thenReturn(Optional.of(ownOrder));

        assertThatThrownBy(() -> sellerOrderService.updateSellerOrderStatus("order-1", SellerOrderStatus.CREATED))
                .isInstanceOf(IllegalArgumentException.class);

        verify(sellerOrderRepository, never()).save(any());
    }

    @Test
    void updateSellerOrderStatus_throwsIllegalArgumentException_whenAlreadyShippedToWarehouse() {
        // Endstatus – kein weiterer Übergang erlaubt
        ownOrder.setStatus(SellerOrderStatus.SHIPPED_TO_WAREHOUSE);

        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("order-1")).thenReturn(Optional.of(ownOrder));

        assertThatThrownBy(() -> sellerOrderService.updateSellerOrderStatus("order-1", SellerOrderStatus.CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class);

        verify(sellerOrderRepository, never()).save(any());
    }

    @Test
    void updateSellerOrderStatus_throwsOrderNotFoundException_whenOrderDoesNotExist() {
        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerOrderService.updateSellerOrderStatus("unknown", SellerOrderStatus.CONFIRMED))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void updateSellerOrderStatus_throwsForbiddenAccessException_whenOrderBelongsToOtherSeller() {
        SellerOrder otherOrder = SellerOrder.builder()
                .id("order-2")
                .sellerId("seller-99")
                .status(SellerOrderStatus.CREATED)
                .build();

        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(sellerOrderRepository.findById("order-2")).thenReturn(Optional.of(otherOrder));

        assertThatThrownBy(() -> sellerOrderService.updateSellerOrderStatus("order-2", SellerOrderStatus.CONFIRMED))
                .isInstanceOf(ForbiddenAccessException.class);

        verify(sellerOrderRepository, never()).save(any());
    }
}
