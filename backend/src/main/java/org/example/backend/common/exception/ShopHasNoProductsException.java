package org.example.backend.common.exception;

public class ShopHasNoProductsException extends RuntimeException {
    public ShopHasNoProductsException(String message) {
        super(message);
    }
}
