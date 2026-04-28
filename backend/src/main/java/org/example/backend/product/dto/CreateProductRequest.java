package org.example.backend.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProductRequest(
        @NotBlank(message = "Produktname ist erforderlich")
        String name,

        @NotBlank(message = "Beschreibung ist erforderlich")
        String description,

        @NotNull(message = "Preis ist erforderlich")
        @DecimalMin(value = "0.01", message = "Preis muss größer als 0 sein")
        @Digits(integer = 8, fraction = 2, message = "Preis darf maximal 2 Nachkommastellen haben")
        BigDecimal price,

        @NotBlank(message = "Kategorie ist erforderlich")
        String category,

        @NotBlank(message = "Bild ist erforderlich")
        String imageUrl,

        @NotNull(message = "Produktionsdatum ist erforderlich")
        LocalDate productionDate,

        @NotNull(message = "Mindesthaltbarkeitsdatum ist erforderlich")
        LocalDate bestBeforeDate,

        @NotNull(message = "Bestand ist erforderlich")
        @Min(value = 0, message = "Bestand darf nicht negativ sein")
        Integer stockQuantity
) {}