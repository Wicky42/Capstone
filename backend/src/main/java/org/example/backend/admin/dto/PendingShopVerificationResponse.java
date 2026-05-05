package org.example.backend.admin.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class PendingShopVerificationResponse {
    String shopId;
    String shopName;
    String shopDescription;
    String sellerId;
    String sellerName;
    String sellerEmail;
    String shopStatus;
    boolean onboardingCompleted;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
