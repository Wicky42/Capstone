package org.example.backend.order.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.common.exception.OrderNotFoundException;
import org.example.backend.order.dto.SellerOrderResponse;
import org.example.backend.order.model.SellerOrder;
import org.example.backend.order.model.SellerOrderStatus;
import org.example.backend.order.repository.SellerOrderRepository;
import org.example.backend.user.model.Seller;
import org.example.backend.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final UserService userService;
    private final SellerOrderRepository sellerOrderRepository;

    /** Erlaubte Statusübergänge für Seller */
    private static final Map<SellerOrderStatus, SellerOrderStatus> ALLOWED_TRANSITIONS = Map.of(
            SellerOrderStatus.CREATED,          SellerOrderStatus.CONFIRMED,
            SellerOrderStatus.CONFIRMED,        SellerOrderStatus.IN_PREPARATION,
            SellerOrderStatus.IN_PREPARATION,   SellerOrderStatus.SHIPPED_TO_WAREHOUSE
    );

    public List<SellerOrderResponse> getAllOrdersForCurrentSeller() {
        Seller currentSeller = userService.getCurrentSeller();
        return sellerOrderRepository.getSellerOrderBySellerId(currentSeller.getId())
                .stream()
                .map(SellerOrderResponse::from)
                .toList();
    }

    public SellerOrderResponse getSellerOrderById(String id) {
        return SellerOrderResponse.from(findAndVerifyOwnership(id));
    }

    public SellerOrderResponse updateSellerOrderStatus(String id, SellerOrderStatus newStatus) {
        SellerOrder sellerOrder = findAndVerifyOwnership(id);
        SellerOrderStatus currentStatus = sellerOrder.getStatus();
        SellerOrderStatus expectedNext = ALLOWED_TRANSITIONS.get(currentStatus);

        if (expectedNext == null || !expectedNext.equals(newStatus)) {
            throw new IllegalArgumentException(
                    "Ungültiger Statusübergang von " + currentStatus + " nach " + newStatus);
        }

        sellerOrder.setStatus(newStatus);
        sellerOrder.setUpdatedAt(Instant.now());
        return SellerOrderResponse.from(sellerOrderRepository.save(sellerOrder));
    }

    // ── private helper ────────────────────────────────────────────────────────

    private SellerOrder findAndVerifyOwnership(String id) {
        Seller currentSeller = userService.getCurrentSeller();
        SellerOrder sellerOrder = sellerOrderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException("SellerOrder mit ID " + id + " nicht gefunden"));
        if (!sellerOrder.getSellerId().equals(currentSeller.getId())) {
            throw new ForbiddenAccessException("Kein Zugriff auf diese Bestellung");
        }
        return sellerOrder;
    }
}
