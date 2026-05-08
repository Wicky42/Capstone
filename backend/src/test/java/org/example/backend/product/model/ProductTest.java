package org.example.backend.product.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .stockQuantity(100)
                .build();
    }

    @Test
    void setStockQuantity_shouldSetNewStock_whenGivenValidValue(){
        product.setStockQuantity(50);
        assertEquals(50, product.getStockQuantity());
    }

    @Test
    void setStockQuantity_shouldThrowIllegalArgumentException_whenCalledWithNegativeValue(){
        assertThrows(IllegalArgumentException.class, () -> product.setStockQuantity(-1));
    }

    @Test
    void decreaseStock_shouldDecreaseStockByGivenQuantity_whenCalledWithValidQuantity(){
        product.decreaseStock(10);
        assertEquals(90, product.getStockQuantity());
    }

    @Test
    void decreaseStock_shouldThrowIllegalArgumentException_whenCalledWithZeroOrNegativeQuantity(){
        assertThrows(IllegalArgumentException.class, () -> product.decreaseStock(0));
        assertThrows(IllegalArgumentException.class, () -> product.decreaseStock(-1));
    }

    @Test
    void decreaseStock_shouldThrowIllegalStateException_whenCalledWithQuantityGreaterThanStock(){
        assertThrows(IllegalStateException.class, () -> product.decreaseStock(101));
    }

    @Test
    void increaseStock_shouldIncreaseStockByGivenQuantity_whenCalledWithValidQuantity(){
        product.increaseStock(10);
        assertEquals(110, product.getStockQuantity());
    }

    @Test
    void increaseStock_shouldThrowIllegalArgumentException_whenCalledWithZeroOrNegativeQuantity(){
        assertThrows(IllegalArgumentException.class, () -> product.increaseStock(0));
        assertThrows(IllegalArgumentException.class, () -> product.increaseStock(-1));
    }

    @Test
    void updatePrice_shouldUpdatePrice_whenCalledWithValidPrice(){
        product.updatePrice(new java.math.BigDecimal("19.99"));
        assertEquals(new java.math.BigDecimal("19.99"), product.getPrice());
    }


}