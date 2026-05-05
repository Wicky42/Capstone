package org.example.backend.product.dto;

import org.example.backend.product.model.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductResponseTest {

    private final Product validProduct = Product.builder()
            .id("1L")
            .name("Test Product")
            .description("This is a test product.")
            .price(new java.math.BigDecimal("9.99"))
            .category("Test Category")
            .imageId(null)
            .productionDate(java.time.LocalDate.of(2024, 1, 1))
            .bestBeforeDate(java.time.LocalDate.of(2024, 12, 31))
            .stockQuantity(100)
            .build();

    @Test
    void fromCeatesAValidResponseDto() {
        ProductResponse response = ProductResponse.from(validProduct);

        assertEquals(validProduct.getId(), response.id());
        assertEquals(validProduct.getName(), response.name());
        assertEquals(validProduct.getDescription(), response.description());
        assertEquals(validProduct.getPrice(), response.price());
        assertEquals(validProduct.getCategory(), response.category());
        assertEquals(validProduct.getImageId(), response.imageUrl());
        assertEquals(validProduct.getProductionDate(), response.productionDate());
        assertEquals(validProduct.getBestBeforeDate(), response.bestBeforeDate());
        assertEquals(validProduct.getStockQuantity(), response.stockQuantity());
    }
}