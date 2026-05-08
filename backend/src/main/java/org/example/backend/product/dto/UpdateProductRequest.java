package org.example.backend.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProductRequest(

        @Size(min = 2, max = 120, message = "Produktname muss zwischen 2 und 120 Zeichen lang sein")
        String name,

        @Size(min = 10, max = 2000, message = "Beschreibung muss zwischen 10 und 2000 Zeichen lang sein")
        String description,

        @DecimalMin(value = "0.01", message = "Preis muss größer als 0 sein")
        @Digits(integer = 8, fraction = 2, message = "Preis darf maximal 2 Nachkommastellen haben")
        BigDecimal price,

        @Size(min = 2, max = 80, message = "Kategorie muss zwischen 2 und 80 Zeichen lang sein")
        String category,

        @Size(max = 500, message = "Bild-URL darf maximal 500 Zeichen lang sein")
        String imageUrl,

        LocalDate productionDate,

        LocalDate bestBeforeDate,

        @Min(value = 0, message = "Bestand darf nicht negativ sein")
        Integer stockQuantity
) {
}