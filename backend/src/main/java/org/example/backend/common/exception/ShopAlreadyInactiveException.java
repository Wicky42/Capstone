package org.example.backend.common.exception;

public class ShopAlreadyInactiveException extends RuntimeException {
    public ShopAlreadyInactiveException(String message) {
        super(message);
    }
}
