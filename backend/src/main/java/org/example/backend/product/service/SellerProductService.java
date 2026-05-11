package org.example.backend.product.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.common.exception.ProductImageNotFoundException;
import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.common.util.SlugUtils;
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
public class SellerProductService {

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
                .slug(generateSlug(currentShop.id(), currentShop.slug(), productRequest.name()))
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

        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse updateProductForCurrentSeller(String productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        Seller currentSeller = userService.getCurrentSeller();

        if (!product.getSellerId().equals(currentSeller.getId())) {
            throw new ForbiddenAccessException("Zugriff verweigert: Sie können nur Ihre eigenen Produkte aktualisieren.");
        }
        validateUpdateProductRequest(product, request);

        if (request.name() != null && !request.name().equals(product.getName())) {
            ShopResponse currentShop = shopService.getCurrentSellerShop();
            product.setName(request.name());
            product.setSlug(generateSlug(currentShop.id(), currentShop.slug(), request.name()));
        } else if (request.name() != null) {
            product.setName(request.name());
        }

        if (request.description()    != null) product.setDescription(request.description());
        if (request.price()          != null) product.setPrice(request.price().setScale(2, RoundingMode.HALF_UP));
        if (request.category()       != null) product.setCategory(request.category());
        if (request.imageUrl()       != null) product.setImageId(request.imageUrl());
        if (request.productionDate() != null) product.setProductionDate(request.productionDate());
        if (request.bestBeforeDate() != null) product.setBestBeforeDate(request.bestBeforeDate());
        if (request.stockQuantity()  != null) product.setStockQuantity(request.stockQuantity());

        product.setUpdatedAt(LocalDateTime.now());
        return ProductResponse.from(productRepository.save(product));
    }

    public void deactivateProductForCurrentSeller(String productId) {
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        Seller currentSeller = userService.getCurrentSeller();

        if (product.getSellerId().equals(currentSeller.getId())) {
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
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        if (!product.getSellerId().equals(currentSeller.getId())) {
            throw new ForbiddenAccessException("Zugriff verweigert: Dieses Produkt gehört nicht zu Ihrem Shop.");
        }
        return ProductResponse.from(product);
    }

    public ProductResponse uploadProductImage(String productId, MultipartFile file) {
        Seller currentSeller = userService.getCurrentSeller();
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        if (!product.getSellerId().equals(currentSeller.getId())) {
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
            return ProductResponse.from(productRepository.save(product));
        } catch (IOException e) {
            throw new ProductImageNotFoundException("Das Bild konnte nicht hochgeladen werden." + e.getMessage());
        }
    }

    public ProductResponse activateProductForCurrentSeller(String productId) {
        Seller currentSeller = userService.getCurrentSeller();
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        if (!product.getSellerId().equals(currentSeller.getId())) {
            throw new ForbiddenAccessException("Sie haben keine Berechtigung dieses Produktbild abzurufen");
        }
        if (product.getStatus() == ProductStatus.ACTIVE) {
            throw new IllegalStateException("Produkt ist bereits aktiv");
        }
        if (product.getStatus() == ProductStatus.RECALLED) {
            throw new IllegalStateException("Produkt kann nicht aktiviert werden, da es zurückgerufen wurde");
        }
        product.setStatus(ProductStatus.ACTIVE);
        product.setUpdatedAt(LocalDateTime.now());
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductImage getProductImageForCurrentSeller(String productId) {
        Seller currentSeller = userService.getCurrentSeller();
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        if (!product.getSellerId().equals(currentSeller.getId())) {
            throw new ForbiddenAccessException("Sie haben keine Berechtigung dieses Produktbild abzurufen");
        }
        return productImageRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductImageNotFoundException("Produktbild nicht gefunden"));
    }

    // ------------ HELPER

    private void validateCreateProductRequest(CreateProductRequest request) {
        if (request.price() == null || request.price().signum() <= 0)
            throw new IllegalArgumentException("Preis muss größer als 0 sein");
        if (request.stockQuantity() == null || request.stockQuantity() < 0)
            throw new IllegalArgumentException("Bestand darf nicht negativ sein");
        if (request.productionDate() != null && request.bestBeforeDate() != null
                && request.bestBeforeDate().isBefore(request.productionDate()))
            throw new IllegalArgumentException("Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen");
    }

    private void validateUpdateProductRequest(Product product, UpdateProductRequest request) {
        if (request.price() != null && request.price().signum() <= 0)
            throw new IllegalArgumentException("Preis muss größer als 0 sein");
        if (request.stockQuantity() != null && request.stockQuantity() < 0)
            throw new IllegalArgumentException("Bestand darf nicht negativ sein");

        LocalDate newProductionDate = request.productionDate() != null
                ? request.productionDate() : product.getProductionDate();
        LocalDate newBestBeforeDate = request.bestBeforeDate() != null
                ? request.bestBeforeDate() : product.getBestBeforeDate();

        if (newProductionDate != null && newBestBeforeDate != null
                && newBestBeforeDate.isBefore(newProductionDate))
            throw new IllegalArgumentException("Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen");
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Bild darf nicht leer sein");
        if (file.getSize() > 5 * (long) 1024 * 1024)
            throw new IllegalArgumentException("Bilder dürfen nicht größer als 5 MB sein");
        String ct = file.getContentType();
        if (ct == null || !(ct.equals("image/jpeg") || ct.equals("image/png") || ct.equals("image/webp")))
            throw new IllegalArgumentException("Nur JPEG, PNG and WEBP Formate sind erlaubt");
    }

    private String generateSlug(String shopId, String shopSlug, String productName) {
        String base = SlugUtils.normalize(shopSlug) + "-" + SlugUtils.normalize(productName);
        if (!productRepository.existsByShopIdAndSlug(shopId, base)) return base;
        int counter = 2;
        String candidate;
        do { candidate = base + "-" + counter++; }
        while (productRepository.existsByShopIdAndSlug(shopId, candidate));
        return candidate;
    }
}

