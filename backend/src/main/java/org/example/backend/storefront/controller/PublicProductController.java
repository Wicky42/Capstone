package org.example.backend.storefront.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.ProductImage;
import org.example.backend.product.service.PublicProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final PublicProductService publicProductService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> searchActiveProducts(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        if (query != null && !query.isBlank()) {
            return ResponseEntity.ok(publicProductService.searchActiveProducts(query, pageable));
        }
        return ResponseEntity.ok(publicProductService.findAllActiveProducts(pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getActiveProductById(
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(publicProductService.getActiveProductById(productId));
    }

    @GetMapping("/{productId}/image")
    public ResponseEntity<byte[]> getProductImage(
            @PathVariable String productId
    ) {
        ProductImage productImage = publicProductService.getProductImage(productId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(productImage.getContentType()))
                .body(productImage.getData());
    }
}
