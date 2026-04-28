package org.example.backend.product.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String sellerId;
    private String shopId;

    private String name;
    private String description;

    @NotNull(message = "Preis ist erforderlich")
    @DecimalMin(value = "0.01", message = "Preis muss größer als 0 sein")
    @Digits(integer = 8, fraction = 2, message = "Preis darf maximal 2 Nachkommastellen haben")
    private BigDecimal price;

    private String category;

    private String imgUrl;
    private LocalDate productionDate;
    private LocalDate bestBeforeDate;

    @Min(value = 0, message = "Bestand darf nicht negativ sein")
    @NotNull(message = "Bestand ist erforderlich")
    private Integer stockQuantity;
    private ProductStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Bestand darf nicht negativ sein");
        }
        this.stockQuantity = stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Menge muss größer als 0 sein");
        }

        if (this.stockQuantity - quantity < 0) {
            throw new IllegalStateException("Nicht genügend Bestand verfügbar");
        }

        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Menge muss größer als 0 sein");
        }

        this.stockQuantity += quantity;
    }

    public void updatePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Preis muss größer als 0 sein");
        }

        this.price = price;
    }
}
