package org.example.backend.storefront.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.ProductCategory;
import org.example.backend.product.service.PublicProductService;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.service.ShopService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorefrontService {

    private final PublicProductService publicProductService;
    private final ShopService shopService;

    /** showByProducts: alle aktiven Produkte, optional gefiltert per Suchbegriff und/oder Kategorie. */
    public Page<ProductResponse> getProductView(String query, ProductCategory category, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return publicProductService.searchActiveProducts(query, category, pageable);
        }
        return publicProductService.findAllActiveProducts(category, pageable);
    }

    /** showBySellers: Liste aller aktiven Shops. */
    public Page<ShopResponse> getShopView(Pageable pageable) {
        return shopService.getActiveShops(pageable);
    }
}
