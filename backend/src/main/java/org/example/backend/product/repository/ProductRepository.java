package org.example.backend.product.repository;

import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Seller-Katalog: "Meine Produkte"
    Page<Product> findBySellerId(String sellerId, Pageable pageable);

    // Shop-Katalog: "Produkte dieses Shops"
    List<Product> findByShopId(String shopId);

    // Öffentliche Produktliste nach Status, z. B. ACTIVE
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    // Öffentlicher Shop-Katalog, nur aktive Produkte
    List<Product> findByShopIdAndStatus(String shopId, ProductStatus status);

    // Onboarding: Hat der Shop schon Produkte?
    boolean existsByShopId(String shopId);

    // Security / Ownership
    boolean existsByIdAndSellerId(String productId, String sellerId);

    Page<Product> findBySellerIdAndStatus(String sellerId, ProductStatus status, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndStatus(String name, ProductStatus status, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndSellerIdAndStatus(
            String name,
            String sellerId,
            ProductStatus status,
            Pageable pageable
    );
}
