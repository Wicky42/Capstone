package org.example.backend.common.exception;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(){
        super("Produkt nicht gefunden");
    }
}
