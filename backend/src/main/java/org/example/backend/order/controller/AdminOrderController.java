package org.example.backend.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.order.dto.AdminFulfillmentOrderDetail;
import org.example.backend.order.dto.FulfillmentOrderResponse;
import org.example.backend.order.model.FulfillmentOrderStatus;
import org.example.backend.order.service.AdminOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<List<FulfillmentOrderResponse>> getAllFulfillmentOrders() {
        return ResponseEntity.ok(adminOrderService.getAllFulfillmentOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminFulfillmentOrderDetail> getFulfillmentOrderDetail(@PathVariable String id) {
        return ResponseEntity.ok(adminOrderService.getFulfillmentOrderDetail(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FulfillmentOrderResponse> updateFulfillmentOrderStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        FulfillmentOrderStatus newStatus = FulfillmentOrderStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(adminOrderService.updateFulfillmentOrderStatus(id, newStatus));
    }
}

