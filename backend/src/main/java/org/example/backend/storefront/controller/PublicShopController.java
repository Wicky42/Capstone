package org.example.backend.storefront.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.service.PublicProductService;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.service.ShopService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/shops")
@RequiredArgsConstructor
public class PublicShopController {

    private final ShopService shopService;
    private final PublicProductService publicProductService;

    /** GET /api/public/shops/{shopId} — Shop-Detailseite per ID */
    @GetMapping("/{shopId}")
    public ResponseEntity<ShopResponse> getActiveShopById(
            @PathVariable String shopId
    ) {
        return ResponseEntity.ok(shopService.getActiveShopById(shopId));
    }

    /** GET /api/public/shops/by-slug/{slug} — Shop-Detailseite per Slug */
    @GetMapping("/by-slug/{slug}")
    public ResponseEntity<ShopResponse> getActiveShopBySlug(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(shopService.getActiveShopBySlug(slug));
    }

    /** GET /api/public/shops/{shopId}/products — Aktive Produkte eines Shops */
    @GetMapping("/{shopId}/products")
    public ResponseEntity<Page<ProductResponse>> getActiveProductsByShop(
            @PathVariable String shopId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(publicProductService.getActiveProductsByShop(shopId, pageable));
    }
}
