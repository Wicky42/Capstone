package org.example.backend.order.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private String productId;
    private String productName;
    private double unitPrice;
    private int quantity;
    private String titleImage;
    private String shopId;
    private String sellerId;

    // Optionale Felder (Snapshot)
    private String productDescription;
    private String category;
    private Instant snapshotCreatedAt;
}

