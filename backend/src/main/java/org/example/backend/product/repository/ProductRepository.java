package org.example.backend.product.repository;

import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Seller-Katalog: "Meine Produkte"
    List<Product> findBySellerId(String sellerId);

    // Shop-Katalog: "Produkte dieses Shops"
    List<Product> findByShopId(String shopId);

    // Öffentliche Produktliste nach Status, z. B. ACTIVE
    List<Product> findByStatus(ProductStatus status);

    // Öffentlicher Shop-Katalog, nur aktive Produkte
    List<Product> findByShopIdAndStatus(String shopId, ProductStatus status);

    // Onboarding: Hat der Shop schon Produkte?
    boolean existsByShopId(String shopId);

    // Security / Ownership
    boolean existsByIdAndSellerId(String productId, String sellerId);

}
