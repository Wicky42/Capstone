package org.example.backend.product.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ProductImageNotFoundException;
import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.common.exception.ShopNotFoundException;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductImage;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.repository.ProductImageRepository;
import org.example.backend.product.repository.ProductRepository;
import org.example.backend.shop.service.ShopService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicProductService {

    private final ShopService shopService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    /** Alle aktiven Produkte aus aktiven Shops, optional nach Kategorie gefiltert. */
    public Page<ProductResponse> findAllActiveProducts(Pageable pageable) {
        return findAllActiveProducts(null, pageable);
    }

    public Page<ProductResponse> findAllActiveProducts(String category, Pageable pageable) {
        List<String> activeShopIds = shopService.getActiveShopIds();
        if (category != null && !category.isBlank()) {
            return productRepository
                    .findByCategoryAndStatusAndShopIdIn(category.trim(), ProductStatus.ACTIVE, activeShopIds, pageable)
                    .map(ProductResponse::from);
        }
        return productRepository
                .findByStatusAndShopIdIn(ProductStatus.ACTIVE, activeShopIds, pageable)
                .map(ProductResponse::from);
    }

    /** Produktsuche nach Name, optional nach Kategorie, nur aus aktiven Shops. */
    public Page<ProductResponse> searchActiveProducts(String query, Pageable pageable) {
        return searchActiveProducts(query, null, pageable);
    }

    public Page<ProductResponse> searchActiveProducts(String query, String category, Pageable pageable) {
        List<String> activeShopIds = shopService.getActiveShopIds();
        if (category != null && !category.isBlank()) {
            return productRepository
                    .findByNameContainingIgnoreCaseAndCategoryAndStatusAndShopIdIn(
                            query.trim(), category.trim(), ProductStatus.ACTIVE, activeShopIds, pageable)
                    .map(ProductResponse::from);
        }
        return productRepository
                .findByNameContainingIgnoreCaseAndStatusAndShopIdIn(
                        query.trim(), ProductStatus.ACTIVE, activeShopIds, pageable)
                .map(ProductResponse::from);
    }

    /** Aktive Produkte eines bestimmten aktiven Shops (für Shop-Detailseite). */
    public Page<ProductResponse> getActiveProductsByShop(String shopId, Pageable pageable) {
        if (!shopService.isShopActive(shopId)) {
            throw new ShopNotFoundException("Shop ist nicht öffentlich verfügbar.");
        }
        return productRepository
                .findByShopIdAndStatus(shopId, ProductStatus.ACTIVE, pageable)
                .map(ProductResponse::from);
    }

    /** Einzelnes aktives Produkt — Shop muss ebenfalls aktiv sein. */
    public ProductResponse getActiveProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ProductNotFoundException();
        }
        if (!shopService.isShopActive(product.getShopId())) {
            throw new ProductNotFoundException();
        }
        return ProductResponse.from(product);
    }

    /** Produktbild — nur für aktive Produkte in aktiven Shops. */
    public ProductImage getProductImage(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ProductNotFoundException();
        }
        if (!shopService.isShopActive(product.getShopId())) {
            throw new ProductNotFoundException();
        }
        return productImageRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductImageNotFoundException("Produktbild nicht gefunden"));
    }
}
