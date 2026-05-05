package org.example.backend.shop.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "shops")
public class Shop {

    @Id
    private String id;
    private String sellerId;
    private String name;
    private String description;
    private String logoUrl;
    private String headerImageUrl;
    private String slug;
    private ShopStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
