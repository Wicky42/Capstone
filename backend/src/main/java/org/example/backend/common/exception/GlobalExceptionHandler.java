package org.example.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_KEY = "error";


    @ExceptionHandler(ForbiddenAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleForbiddenAccess(ForbiddenAccessException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleProductNotFound(ProductNotFoundException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }

    @ExceptionHandler(ProductImageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleProductImageNotFound(ProductImageNotFoundException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }
    @ExceptionHandler(ShopNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleShopNotFound(ShopNotFoundException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUserNotFound(UserNotFoundException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }

    @ExceptionHandler(ShopAlreadyActiveException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleShopAlreadyActive(ShopAlreadyActiveException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }

    @ExceptionHandler(SellerOnboardingIncompleteException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleSellerOnboardingIncomplete(SellerOnboardingIncompleteException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }

    @ExceptionHandler(ShopHasNoProductsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleShopHasNoProducts(ShopHasNoProductsException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }

    @ExceptionHandler(InvalidShopStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleInvalidShopSeller(InvalidShopStateException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }

    @ExceptionHandler(ShopRejectedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleInvalidShopSeller(ShopRejectedException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }

    @ExceptionHandler(ShopAlreadyInactiveException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleInvalidShopSeller(ShopAlreadyInactiveException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex) {
        return Map.of(ERROR_KEY, ex.getMessage());
    }
}

