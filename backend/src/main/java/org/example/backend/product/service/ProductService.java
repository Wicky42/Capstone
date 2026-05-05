package org.example.backend.product.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.common.exception.ProductImageNotFoundException;
import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.product.dto.CreateProductRequest;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.dto.UpdateProductRequest;
import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductImage;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.repository.ProductImageRepository;
import org.example.backend.product.repository.ProductRepository;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.service.ShopService;
import org.example.backend.user.model.Seller;
import org.example.backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final UserService userService;
    private final ShopService shopService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

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
                .imageId(productRequest.imageUrl())
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

    public ProductResponse updateProductForCurrentSeller(String productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ProductNotFoundException("Produkt nicht gefunden"));

        Seller currentSeller = userService.getCurrentSeller();

        if(!product.getSellerId().equals(currentSeller.getId())){
            throw new ForbiddenAccessException("Zugriff verweigert: Sie können nur Ihre eigenen Produkte aktualisieren.");
        }
        validateUpdateProductRequest(product, request);

        if (request.name() != null) {
            product.setName(request.name());
        }

        if (request.description() != null) {
            product.setDescription(request.description());
        }

        if (request.price() != null) {
            product.setPrice(request.price().setScale(2, RoundingMode.HALF_UP));
        }

        if (request.category() != null) {
            product.setCategory(request.category());
        }

        if (request.imageUrl() != null) {
            product.setImageId(request.imageUrl());
        }

        if (request.productionDate() != null) {
            product.setProductionDate(request.productionDate());
        }

        if (request.bestBeforeDate() != null) {
            product.setBestBeforeDate(request.bestBeforeDate());
        }

        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
        }

        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);

    }

    public void deactivateProductForCurrentSeller(String productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ProductNotFoundException("Produkt nicht gefunden"));
        Seller currentSeller = userService.getCurrentSeller();

        if(product.getSellerId().equals(currentSeller.getId())){
            product.setStatus(ProductStatus.INACTIVE);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        } else {
            throw new ForbiddenAccessException("Zugriff verweigert: Sie können nur Ihre eigenen Produkte deaktivieren.");
        }
    }

    public Page<ProductResponse> getCurrentSellerProducts(Pageable pageable, ProductStatus status) {
        Seller currentSeller = userService.getCurrentSeller();
        if (status != null) {
            return productRepository.findBySellerIdAndStatus(currentSeller.getId(), status, pageable)
                    .map(ProductResponse::from);
        }
        return productRepository.findBySellerId(currentSeller.getId(), pageable)
                .map(ProductResponse::from);
    }

    public ProductResponse getSellerProductById(String productId) {
        Seller currentSeller = userService.getCurrentSeller();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produkt nicht gefunden"));
        if (!product.getSellerId().equals(currentSeller.getId())) {
            throw new ForbiddenAccessException("Zugriff verweigert: Dieses Produkt gehört nicht zu Ihrem Shop.");
        }
        return ProductResponse.from(product);
    }

    public ProductResponse getProductById(String productId) {
        return productRepository.findById(productId)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException("Produkt nicht gefunden"));
    }

    public Page<ProductResponse> searchProducts(String query, String sellerId, boolean active, Pageable pageable) {
        ProductStatus status = active ? ProductStatus.ACTIVE : ProductStatus.INACTIVE;

        boolean hasQuery = query != null && !query.isBlank();
        boolean hasSellerId = sellerId != null && !sellerId.isBlank();

        Page<Product> products;

        if (hasQuery && hasSellerId) {
            products = productRepository.findByNameContainingIgnoreCaseAndSellerIdAndStatus(
                    query.trim(),
                    sellerId.trim(),
                    status,
                    pageable
            );
        } else if (hasQuery) {
            products = productRepository.findByNameContainingIgnoreCaseAndStatus(
                    query.trim(),
                    status,
                    pageable
            );
        } else if (hasSellerId) {
            products = productRepository.findBySellerIdAndStatus(
                    sellerId.trim(),
                    status,
                    pageable
            );
        } else {
            products = productRepository.findByStatus(status, pageable);
        }

        return products.map(ProductResponse::from);
    }

    public ProductResponse getActiveProductById(String productId) {
        ProductResponse response = getProductById(productId);
        if(response.status() != ProductStatus.ACTIVE){
            throw new ProductNotFoundException("Produkt nicht gefunden");
        }
        return response;
    }

    public ProductResponse uploadProductImage(String productId, MultipartFile file){
        Seller currentSeller = userService.getCurrentSeller();

        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ProductNotFoundException("Produkt nicht gefunden"));

        if(!product.getSellerId().equals(currentSeller.getId())){
            throw new ForbiddenAccessException("Sie haben keine Berechtigung dieses Produkt zu bearbeiten");
        }

        validateImage(file);

        try {
            productImageRepository.deleteByProductId(productId);

            ProductImage image = ProductImage.builder()
                    .productId(product.getId())
                    .sellerId(currentSeller.getId())
                    .filename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .data(file.getBytes())
                    .build();

            ProductImage savedImage = productImageRepository.save(image);

            product.setImageId(savedImage.getId());

            Product savedProduct = productRepository.save(product);

            return ProductResponse.from(savedProduct);

        } catch (IOException e) {
            throw new RuntimeException("Das Bild konnte nicht hochgeladen werden.", e);
        }
    }

    public ProductResponse activateProductForCurrentSeller(String productId) {
        Seller currentSeller = userService.getCurrentSeller();

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException("Produkt nicht gefunden"));

        if (!product.getSellerId().equals(currentSeller.getId())) {
            throw new ForbiddenAccessException("Sie haben keine Berechtigung dieses Produktbild abzurufen");
        }

        if(product.getStatus() == ProductStatus.ACTIVE){
            throw new IllegalStateException("Produkt ist bereits aktiv");
        }

        if(product.getStatus() == ProductStatus.RECALLED){
            throw new IllegalStateException("Produkt kann nicht aktiviert werden, da es zurückgerufen wurde");
        }

        product.setStatus(ProductStatus.ACTIVE);
        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    public ProductImage getProductImage(String productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ProductNotFoundException("Produkt nicht gefunden"));

        if(product.getStatus() != ProductStatus.ACTIVE){
            throw new ProductNotFoundException("Produkt nicht gefunden");
        }

        return productImageRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductImageNotFoundException("Produktbild nicht gefunden"));
    }

    public ProductImage getProductImageForCurrentSeller(String productId) {
        Seller currentSeller = userService.getCurrentSeller();

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException("Produkt nicht gefunden"));

        if (!product.getSellerId().equals(currentSeller.getId())) {
            throw new ForbiddenAccessException("Sie haben keine Berechtigung dieses Produktbild abzurufen");
        }

        return productImageRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductImageNotFoundException("Produktbild nicht gefunden"));
    }



    //------------ HELPER
    private void validateCreateProductRequest(CreateProductRequest request) {
        if (request.price() == null || request.price().signum() <= 0) {
            throw new IllegalArgumentException("Preis muss größer als 0 sein");
        }

        if (request.stockQuantity() == null || request.stockQuantity() < 0) {
            throw new IllegalArgumentException("Bestand darf nicht negativ sein");
        }

        if (request.productionDate() != null
                && request.bestBeforeDate() != null
                && request.bestBeforeDate().isBefore(request.productionDate())) {
            throw new IllegalArgumentException("Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen");
        }
    }

    private void validateUpdateProductRequest(Product product, UpdateProductRequest request) {
        if (request.price() != null && request.price().signum() <= 0) {
            throw new IllegalArgumentException("Preis muss größer als 0 sein");
        }

        if (request.stockQuantity() != null && request.stockQuantity() < 0) {
            throw new IllegalArgumentException("Bestand darf nicht negativ sein");
        }

        LocalDate newProductionDate = request.productionDate() != null
                ? request.productionDate()
                : product.getProductionDate();

        LocalDate newBestBeforeDate = request.bestBeforeDate() != null
                ? request.bestBeforeDate()
                : product.getBestBeforeDate();

        if (newProductionDate != null
                && newBestBeforeDate != null
                && newBestBeforeDate.isBefore(newProductionDate)) {
            throw new IllegalArgumentException("Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Bild darf nicht leer sein");
        }

        long maxSize = 5 * 1024 * 1024; // 5 MB

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Bilder dürfen nicht größer als 5 MB sein");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !(contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Nur JPEG, PNG and WEBP Formate sind erlaubt");
        }
    }



}
