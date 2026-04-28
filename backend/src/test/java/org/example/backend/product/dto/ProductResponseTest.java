package org.example.backend.product.dto;

import org.example.backend.product.model.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductResponseTest {

    private final Product VALID_PRODUCT = Product.builder()
            .id("1L")
            .name("Test Product")
            .description("This is a test product.")
            .price(new java.math.BigDecimal("9.99"))
            .category("Test Category")
            .imageUrl("http://example.com/image.jpg")
            .productionDate(java.time.LocalDate.of(2024, 1, 1))
            .bestBeforeDate(java.time.LocalDate.of(2024, 12, 31))
            .stockQuantity(100)
            .build();

    @Test
    void fromCeatesAValidResponseDto() {
        ProductResponse response = ProductResponse.from(VALID_PRODUCT);

        assertEquals(VALID_PRODUCT.getId(), response.id());
        assertEquals(VALID_PRODUCT.getName(), response.name());
        assertEquals(VALID_PRODUCT.getDescription(), response.description());
        assertEquals(VALID_PRODUCT.getPrice(), response.price());
        assertEquals(VALID_PRODUCT.getCategory(), response.category());
        assertEquals(VALID_PRODUCT.getImageUrl(), response.imageUrl());
        assertEquals(VALID_PRODUCT.getProductionDate(), response.productionDate());
        assertEquals(VALID_PRODUCT.getBestBeforeDate(), response.bestBeforeDate());
        assertEquals(VALID_PRODUCT.getStockQuantity(), response.stockQuantity());
    }
}