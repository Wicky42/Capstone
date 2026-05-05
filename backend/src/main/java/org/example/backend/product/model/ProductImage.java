package org.example.backend.product.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    private String id;
    private String productId;
    private String sellerId;
    private String filename;
    private String contentType;
    private long size;
    private byte[] data;

}
