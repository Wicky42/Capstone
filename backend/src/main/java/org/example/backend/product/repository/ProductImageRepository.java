package org.example.backend.product.repository;

import org.example.backend.product.model.ProductImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductImageRepository extends MongoRepository<ProductImage, String> {
    Optional<ProductImage> findByProductId(String productId);

    void deleteByProductId(String productId);
}
