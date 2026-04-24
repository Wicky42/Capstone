package org.example.backend.shop.repository;

import org.example.backend.shop.model.Shop;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends MongoRepository<Shop, String> {
    Optional<Shop> findBySellerId(String sellerId);
    boolean existsBySellerId(String sellerId);
    Optional<Shop> findBySlug(String slug);
    boolean existsByName(String name);
}
