package org.example.backend.admin.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.backend.admin.dto.PendingShopVerificationResponse;
import org.example.backend.admin.service.AdminShopVerificationService;
import org.example.backend.shop.dto.ShopResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/shops")
@RequiredArgsConstructor
public class AdminShopVerificationController {

    private final AdminShopVerificationService adminShopVerificationService;

    @GetMapping("/pending-verification")
    public ResponseEntity<List<PendingShopVerificationResponse>> getPendingShopVerifications() {
        return ResponseEntity.ok(adminShopVerificationService.getPendingShopVerifications());
    }

    @PutMapping("/{shopId}/activate")
    public ResponseEntity<ShopResponse> activateShop(@PathVariable @NotBlank String shopId) {
        return ResponseEntity.ok(adminShopVerificationService.activateShop(shopId));
    }

    @PutMapping("/{shopId}/deactivate")
    public ResponseEntity<ShopResponse> deactivateShop(@PathVariable @NotBlank String shopId) {
        return ResponseEntity.ok(adminShopVerificationService.deactivateShop(shopId));
    }

    @PutMapping("/{shopId}/reject")
    public ResponseEntity<ShopResponse> rejectShop(@PathVariable @NotBlank String shopId) {
        return ResponseEntity.ok(adminShopVerificationService.rejectShop(shopId));
    }

}
