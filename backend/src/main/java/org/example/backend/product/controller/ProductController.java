package org.example.backend.product.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> searchActiveProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sellerId,
            @PageableDefault(size = 20)Pageable pageable
    ) {
        return ResponseEntity.ok(
                productService.searchProducts(query, sellerId, true, pageable)
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getActiveProductById(
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(productService.getActiveProductById(productId));
    }

}
