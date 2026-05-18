package org.example.backend.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.order.dto.SellerOrderResponse;
import org.example.backend.order.model.SellerOrderStatus;
import org.example.backend.order.service.SellerOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<SellerOrderResponse>> getAllSellerOrders() {
        return ResponseEntity.ok(sellerOrderService.getAllOrdersForCurrentSeller());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SellerOrderResponse> getSellerOrderById(@PathVariable String id) {
        return ResponseEntity.ok(sellerOrderService.getSellerOrderById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SellerOrderResponse> updateSellerOrderStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        SellerOrderStatus newStatus = SellerOrderStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(sellerOrderService.updateSellerOrderStatus(id, newStatus));
    }
}
