package org.example.backend.product.dto;

import lombok.Builder;
import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ProductResponse(
        String id,
        String sellerId,
        String shopId,
        String name,
        String description,
        BigDecimal price,
        String category,
        String imageUrl,
        LocalDate productionDate,
        LocalDate bestBeforeDate,
        Integer stockQuantity,
        ProductStatus status
    ){

    public static ProductResponse from(Product product){
        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .shopId(product.getShopId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .productionDate(product.getProductionDate())
                .bestBeforeDate(product.getBestBeforeDate())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .build();
    }
}
