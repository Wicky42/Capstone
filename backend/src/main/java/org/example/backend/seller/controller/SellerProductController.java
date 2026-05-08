package org.example.backend.seller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.product.dto.CreateProductRequest;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.dto.UpdateProductRequest;
import org.example.backend.product.model.ProductImage;
import org.example.backend.product.service.ProductService;
import org.example.backend.product.model.ProductStatus;
import org.springframework.data.domain.Page;import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getCurrentSellerProducts(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) ProductStatus status
    ) {
        return ResponseEntity.ok(productService.getCurrentSellerProducts(pageable, status));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getSellerProductById(
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(productService.getSellerProductById(productId));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProductForCurrentSeller(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = productService.createProductForCurrentSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productId}/image")
    public ResponseEntity<byte[]> getProductImageForCurrentSeller(
            @PathVariable String productId
    ) {
        ProductImage productImage = productService.getProductImageForCurrentSeller(productId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(productImage.getContentType()))
                .body(productImage.getData());
    }

    @PostMapping("/{productId}/image")
    public ResponseEntity<ProductResponse> uploadProductImage(
            @PathVariable String productId,
            @RequestParam("file")MultipartFile file
            ) {
        return ResponseEntity.ok(productService.uploadProductImage(productId, file));
    }
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProductForCurrentSeller(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity.ok(
                productService.updateProductForCurrentSeller(productId, request)
        );
    }

    @PutMapping("/{productId}/activate")
    public ResponseEntity<ProductResponse> activateProductForCurrentSeller(
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(
                productService.activateProductForCurrentSeller(productId)
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deactivateProductForCurrentSeller(
            @PathVariable String productId
    ) {
        productService.deactivateProductForCurrentSeller(productId);
        return ResponseEntity.noContent().build();
    }
}
