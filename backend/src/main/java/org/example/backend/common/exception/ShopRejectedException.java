package org.example.backend.common.exception;

public class ShopRejectedException extends RuntimeException {
    public ShopRejectedException(String message) {
        super(message);
    }
}
