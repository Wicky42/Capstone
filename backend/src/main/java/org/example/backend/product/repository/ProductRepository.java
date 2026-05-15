package org.example.backend.product.repository;

import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductCategory;
import org.example.backend.product.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Seller-Katalog: "Meine Produkte"
    Page<Product> findBySellerId(String sellerId, Pageable pageable);

    // Shop-Katalog: "Produkte dieses Shops"
    List<Product> findByShopId(String shopId);

    // Öffentlicher Shop-Katalog, nur aktive Produkte
    List<Product> findByShopIdAndStatus(String shopId, ProductStatus status);

    // Öffentlicher Shop-Katalog, paginiert (für Shop-Detailseite)
    Page<Product> findByShopIdAndStatus(String shopId, ProductStatus status, Pageable pageable);

    // Onboarding: Hat der Shop schon Produkte?
    boolean existsByShopId(String shopId);

    // Security / Ownership
    boolean existsByIdAndSellerId(String productId, String sellerId);

    // Slug uniqueness within a shop (scoped)
    boolean existsByShopIdAndSlug(String shopId, String slug);

    Page<Product> findBySellerIdAndStatus(String sellerId, ProductStatus status, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndSellerIdAndStatus(
            String name,
            String sellerId,
            ProductStatus status,
            Pageable pageable
    );

    // Öffentliche Storefront: nur Produkte aus aktiven Shops
    Page<Product> findByStatusAndShopIdIn(
            ProductStatus status, Collection<String> shopIds, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndStatusAndShopIdIn(
            String name, ProductStatus status, Collection<String> shopIds, Pageable pageable);

    // Kategoriefilter
    Page<Product> findByCategoryAndStatusAndShopIdIn(
            ProductCategory category, ProductStatus status, Collection<String> shopIds, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndCategoryAndStatusAndShopIdIn(
            String name, ProductCategory category, ProductStatus status, Collection<String> shopIds, Pageable pageable);
}
