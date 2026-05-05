package org.example.backend.common.exception;

public class ProductImageNotFoundException extends RuntimeException {
    public ProductImageNotFoundException(String message) {
        super(message);
    }
}
