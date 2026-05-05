package org.example.backend.common.exception;

public class ShopAlreadyActiveException extends RuntimeException {
    public ShopAlreadyActiveException(String message) {
        super(message);
    }
}
