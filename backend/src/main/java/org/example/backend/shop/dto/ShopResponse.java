package org.example.backend.shop.dto;

import org.example.backend.shop.model.Shop;
import org.example.backend.shop.model.ShopStatus;

import java.time.LocalDateTime;

public record ShopResponse(
        String id,
        String sellerId,
        String name,
        String description,
        String logoUrl,
        String headerImageUrl,
        String slug,
        ShopStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ShopResponse from(Shop shop) {
        return new ShopResponse(
                shop.getId(),
                shop.getSellerId(),
                shop.getName(),
                shop.getDescription(),
                shop.getLogoUrl(),
                shop.getHeaderImageUrl(),
                shop.getSlug(),
                shop.getStatus(),
                shop.getCreatedAt(),
                shop.getUpdatedAt()
        );
    }
}