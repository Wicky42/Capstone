package org.example.backend.storefront.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.storefront.service.StorefrontService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/storefront")
@RequiredArgsConstructor
public class StorefrontController {

    private final StorefrontService storefrontService;

    /**
     * GET /api/public/storefront/products?query=…
     * showByProducts: alle aktiven Produkte, optional mit Suchbegriff.
     */
    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponse>> getProductView(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(storefrontService.getProductView(query, category, pageable));
    }

    /**
     * GET /api/public/storefront/shops
     * showBySellers: alle aktiven Shops.
     */
    @GetMapping("/shops")
    public ResponseEntity<Page<ShopResponse>> getShopView(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(storefrontService.getShopView(pageable));
    }
}
