package org.example.backend.common.exception;

public class InvalidShopStateException extends RuntimeException {
    public InvalidShopStateException(String message) {
        super(message);
    }
}
