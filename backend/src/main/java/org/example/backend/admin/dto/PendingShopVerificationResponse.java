package org.example.backend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
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
