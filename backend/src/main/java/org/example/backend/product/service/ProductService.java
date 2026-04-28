package org.example.backend.product.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.product.dto.CreateProductRequest;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.repository.ProductRepository;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.service.ShopService;
import org.example.backend.user.model.Seller;
import org.example.backend.user.service.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final UserService userService;
    private final ShopService shopService;
    private final ProductRepository productRepository;

    public ProductResponse createProductForCurrentSeller(CreateProductRequest productRequest) {
        validateCreateProductRequest(productRequest);

        Seller currentSeller = userService.getCurrentSeller();
        ShopResponse currentShop = shopService.getCurrentSellerShop();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal normalizedPrice = productRequest.price().setScale(2, RoundingMode.HALF_UP);

        Product product = Product.builder()
                .sellerId(currentSeller.getId())
                .shopId(currentShop.id())
                .name(productRequest.name())
                .description(productRequest.description())
                .price(normalizedPrice)
                .category(productRequest.category())
                .imageUrl(productRequest.imageUrl())
                .productionDate(productRequest.productionDate())
                .bestBeforeDate(productRequest.bestBeforeDate())
                .stockQuantity(productRequest.stockQuantity())
                .status(ProductStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Product response = productRepository.save(product);
        return ProductResponse.from(response);
    }

    //------------ HELPER
    private void validateCreateProductRequest(CreateProductRequest request) {
        if (request.price() == null || request.price().signum() <= 0) {
            throw new IllegalArgumentException("Preis muss größer als 0 sein");
        }

        if (request.stockQuantity() == null || request.stockQuantity() < 0) {
            throw new IllegalArgumentException("Bestand darf nicht negativ sein");
        }

        if (request.bestBeforeDate().isBefore(request.productionDate())) {
            throw new IllegalArgumentException("Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen");
        }
    }

}
