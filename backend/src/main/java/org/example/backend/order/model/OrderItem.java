package org.example.backend.order.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @NotBlank
    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    @Min(1)
    private int quantity;
    private String titleImage;
    private String shopId;
    private String sellerId;

    // Optionale Felder (Snapshot)
    private String productDescription;
    private String category;
    private Instant snapshotCreatedAt;
}

