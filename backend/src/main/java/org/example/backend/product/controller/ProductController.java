package org.example.backend.product.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> searchActiveProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sellerId
    ) {
        return ResponseEntity.ok(
                productService.searchProducts(query, sellerId, true)
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getActiveProductById(
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(productService.getActiveProductById(productId));
    }

}
