package org.example.backend.product.service;

import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.product.dto.CreateProductRequest;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.dto.UpdateProductRequest;
import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductCategory;
import org.example.backend.product.model.ProductImage;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.repository.ProductImageRepository;
import org.example.backend.product.repository.ProductRepository;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.shop.service.ShopService;
import org.example.backend.user.model.Seller;
import org.example.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private UserService userService;

    @Mock
    private ShopService shopService;

    @InjectMocks
    private SellerProductService sellerProductService;

    // CREATE EXISTING PRODUCT HELPER
    private Product createExistingProduct() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 10, 0);

        return Product.builder()
                .id("product-1")
                .sellerId("seller-1")
                .shopId("shop-1")
                .name("Waldhonig")
                .description("Aromatischer Waldhonig")
                .price(new BigDecimal("8.99"))
                .category(ProductCategory.HONIG)
                .imageId("/static/images/honey.jpg")
                .productionDate(LocalDate.of(2026, 4, 1))
                .bestBeforeDate(LocalDate.of(2027, 4, 1))
                .stockQuantity(10)
                .status(ProductStatus.DRAFT)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    //---------------- CREATE PRODUCT FOR CURRENT SELLER
    @Test
    void createProductForCurrentSeller_shouldCreateDraftProductForCurrentSellerShop() {
        // given
        Seller seller = Seller.builder()
                .id("seller-1")
                .build();

        ShopResponse shop = new ShopResponse(
                "shop-1",
                "seller-1",
                "Mein Shop",
                "mein-shop",
                null,
                null,
                "mein-shop",
                ShopStatus.DRAFT,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        CreateProductRequest request = new CreateProductRequest(
                "Waldhonig",
                "Aromatischer Waldhonig aus regionaler Imkerei",
                new BigDecimal("8.999"),
                ProductCategory.HONIG,
                "/static/images/honey.jpg",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 4, 1),
                12
        );

        Product savedProduct = Product.builder()
                .id("product-1")
                .sellerId("seller-1")
                .shopId("shop-1")
                .name("Waldhonig")
                .description("Aromatischer Waldhonig aus regionaler Imkerei")
                .price(new BigDecimal("9.00"))
                .category(ProductCategory.HONIG)
                .imageId("/static/images/honey.jpg")
                .productionDate(LocalDate.of(2026, 4, 1))
                .bestBeforeDate(LocalDate.of(2027, 4, 1))
                .stockQuantity(12)
                .status(ProductStatus.DRAFT)
                .build();

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopService.getCurrentSellerShop()).thenReturn(shop);
        when(productRepository.existsByShopIdAndSlug("shop-1", "mein-shop-waldhonig")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // when
        ProductResponse response = sellerProductService.createProductForCurrentSeller(request);

        // then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product productToSave = productCaptor.getValue();

        assertThat(productToSave.getSellerId()).isEqualTo("seller-1");
        assertThat(productToSave.getShopId()).isEqualTo("shop-1");
        assertThat(productToSave.getName()).isEqualTo("Waldhonig");
        assertThat(productToSave.getSlug()).isEqualTo("mein-shop-waldhonig");
        assertThat(productToSave.getDescription()).isEqualTo("Aromatischer Waldhonig aus regionaler Imkerei");
        assertThat(productToSave.getPrice()).isEqualByComparingTo("9.00");
        assertThat(productToSave.getCategory()).isEqualTo(ProductCategory.HONIG);
        assertThat(productToSave.getImageId()).isEqualTo("/static/images/honey.jpg");
        assertThat(productToSave.getProductionDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(productToSave.getBestBeforeDate()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(productToSave.getStockQuantity()).isEqualTo(12);
        assertThat(productToSave.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(productToSave.getCreatedAt()).isNotNull();
        assertThat(productToSave.getUpdatedAt()).isNotNull();

        assertThat(response.id()).isEqualTo("product-1");
        assertThat(response.sellerId()).isEqualTo("seller-1");
        assertThat(response.shopId()).isEqualTo("shop-1");
        assertThat(response.name()).isEqualTo("Waldhonig");
        assertThat(response.price()).isEqualByComparingTo("9.00");
        assertThat(response.status()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void createProductForCurrentSeller_shouldRejectProductWhenBestBeforeDateIsBeforeProductionDate() {
        // given
        CreateProductRequest request = new CreateProductRequest(
                "Waldhonig",
                "Aromatischer Waldhonig aus regionaler Imkerei",
                new BigDecimal("8.99"),
                ProductCategory.HONIG,
                "/static/images/honey.jpg",
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 1),
                12
        );

        // when / then
        assertThatThrownBy(() -> sellerProductService.createProductForCurrentSeller(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen");

        verify(productRepository, never()).save(any(Product.class));
        verifyNoInteractions(userService);
        verifyNoInteractions(shopService);
    }

    @Test
    void createProductForCurrentSeller_shouldRejectNegativeStockQuantity() {
        // given
        CreateProductRequest request = new CreateProductRequest(
                "Waldhonig",
                "Aromatischer Waldhonig aus regionaler Imkerei",
                new BigDecimal("8.99"),
                ProductCategory.HONIG,
                "/static/images/honey.jpg",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 4, 1),
                -1
        );

        // when / then
        assertThatThrownBy(() -> sellerProductService.createProductForCurrentSeller(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bestand darf nicht negativ sein");

        verify(productRepository, never()).save(any(Product.class));
        verifyNoInteractions(userService);
        verifyNoInteractions(shopService);
    }

    @Test
    void createProductForCurrentSeller_shouldRejectPriceLessThanOrEqualZero() {
        // given
        CreateProductRequest request = new CreateProductRequest(
                "Waldhonig",
                "Aromatischer Waldhonig aus regionaler Imkerei",
                BigDecimal.ZERO,
                ProductCategory.HONIG,
                "/static/images/honey.jpg",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 4, 1),
                12
        );

        // when / then
        assertThatThrownBy(() -> sellerProductService.createProductForCurrentSeller(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Preis muss größer als 0 sein");

        verify(productRepository, never()).save(any(Product.class));
        verifyNoInteractions(userService);
        verifyNoInteractions(shopService);
    }

    //------------- UPDATE PRODUCT FOR CURRENT SELLER
    @Test
    void updateProductForCurrentSeller_shouldUpdateOnlyProvidedFields() {
        // given
        Seller seller = Seller.builder()
                .id("seller-1")
                .build();

        ShopResponse shop = new ShopResponse(
                "shop-1", "seller-1", "Mein Shop", "Beschreibung",
                null, null, "mein-shop", ShopStatus.DRAFT,
                LocalDateTime.now(), LocalDateTime.now()
        );

        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 10, 0);

        Product existingProduct = Product.builder()
                .id("product-1")
                .sellerId("seller-1")
                .shopId("shop-1")
                .name("Alter Honig")
                .description("Alte Beschreibung")
                .price(new BigDecimal("8.99"))
                .category(ProductCategory.HONIG)
                .imageId("/static/images/old.jpg")
                .productionDate(LocalDate.of(2026, 4, 1))
                .bestBeforeDate(LocalDate.of(2027, 4, 1))
                .stockQuantity(10)
                .status(ProductStatus.DRAFT)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        UpdateProductRequest request = new UpdateProductRequest(
                "Neuer Waldhonig",
                null,
                new BigDecimal("9.999"),
                null,
                "/static/images/new.jpg",
                null,
                null,
                15
        );

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(existingProduct));
        when(shopService.getCurrentSellerShop()).thenReturn(shop);
        when(productRepository.existsByShopIdAndSlug("shop-1", "mein-shop-neuer-waldhonig")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ProductResponse response = sellerProductService.updateProductForCurrentSeller("product-1", request);

        // then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getId()).isEqualTo("product-1");
        assertThat(savedProduct.getSellerId()).isEqualTo("seller-1");
        assertThat(savedProduct.getShopId()).isEqualTo("shop-1");

        assertThat(savedProduct.getName()).isEqualTo("Neuer Waldhonig");
        assertThat(savedProduct.getSlug()).isEqualTo("mein-shop-neuer-waldhonig");
        assertThat(savedProduct.getDescription()).isEqualTo("Alte Beschreibung");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo("10.00");
        assertThat(savedProduct.getCategory()).isEqualTo(ProductCategory.HONIG);
        assertThat(savedProduct.getImageId()).isEqualTo("/static/images/new.jpg");
        assertThat(savedProduct.getProductionDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(savedProduct.getBestBeforeDate()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(savedProduct.getStockQuantity()).isEqualTo(15);
        assertThat(savedProduct.getStatus()).isEqualTo(ProductStatus.DRAFT);

        assertThat(savedProduct.getCreatedAt()).isEqualTo(createdAt);
        assertThat(savedProduct.getUpdatedAt()).isAfter(createdAt);

        assertThat(response.id()).isEqualTo("product-1");
        assertThat(response.name()).isEqualTo("Neuer Waldhonig");
        assertThat(response.description()).isEqualTo("Alte Beschreibung");
        assertThat(response.price()).isEqualByComparingTo("10.00");
        assertThat(response.stockQuantity()).isEqualTo(15);
    }

    @Test
    void updateProductForCurrentSeller_shouldThrowNotFoundExceptionWhenProductDoesNotExist() {
        // given
        UpdateProductRequest request = new UpdateProductRequest(
                "Neuer Name",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(productRepository.findById("missing-product")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sellerProductService.updateProductForCurrentSeller("missing-product", request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Produkt nicht gefunden");

        verify(productRepository, never()).save(any(Product.class));
        verifyNoInteractions(userService);
    }

    @Test
    void updateProductForCurrentSeller_shouldRejectUpdateWhenProductBelongsToAnotherSeller() {
        // given
        Seller currentSeller = Seller.builder()
                .id("seller-1")
                .build();

        Product foreignProduct = Product.builder()
                .id("product-1")
                .sellerId("seller-2")
                .shopId("shop-2")
                .name("Fremdes Produkt")
                .description("Beschreibung")
                .price(new BigDecimal("8.99"))
                .category(ProductCategory.HONIG)
                .imageId("/static/images/honey.jpg")
                .productionDate(LocalDate.of(2026, 4, 1))
                .bestBeforeDate(LocalDate.of(2027, 4, 1))
                .stockQuantity(10)
                .status(ProductStatus.DRAFT)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        UpdateProductRequest request = new UpdateProductRequest(
                "Manipulierter Name",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(productRepository.findById("product-1")).thenReturn(Optional.of(foreignProduct));
        when(userService.getCurrentSeller()).thenReturn(currentSeller);

        // when / then
        assertThatThrownBy(() -> sellerProductService.updateProductForCurrentSeller("product-1", request))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage("Zugriff verweigert: Sie können nur Ihre eigenen Produkte aktualisieren.");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductForCurrentSeller_shouldRejectNegativeStockQuantity() {
        // given
        Seller seller = Seller.builder()
                .id("seller-1")
                .build();

        Product existingProduct = createExistingProduct();

        UpdateProductRequest request = new UpdateProductRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                -1
        );

        when(productRepository.findById("product-1")).thenReturn(Optional.of(existingProduct));
        when(userService.getCurrentSeller()).thenReturn(seller);

        // when / then
        assertThatThrownBy(() -> sellerProductService.updateProductForCurrentSeller("product-1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bestand darf nicht negativ sein");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductForCurrentSeller_shouldRejectPriceLessThanOrEqualZero() {
        // given
        Seller seller = Seller.builder()
                .id("seller-1")
                .build();

        Product existingProduct = createExistingProduct();

        UpdateProductRequest request = new UpdateProductRequest(
                null,
                null,
                BigDecimal.ZERO,
                null,
                null,
                null,
                null,
                null
        );

        when(productRepository.findById("product-1")).thenReturn(Optional.of(existingProduct));
        when(userService.getCurrentSeller()).thenReturn(seller);

        // when / then
        assertThatThrownBy(() -> sellerProductService.updateProductForCurrentSeller("product-1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Preis muss größer als 0 sein");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductForCurrentSeller_shouldRejectWhenBestBeforeDateWouldBeBeforeProductionDate() {
        // given
        Seller seller = Seller.builder()
                .id("seller-1")
                .build();

        Product existingProduct = createExistingProduct();

        UpdateProductRequest request = new UpdateProductRequest(
                null,
                null,
                null,
                null,
                null,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 1),
                null
        );

        when(productRepository.findById("product-1")).thenReturn(Optional.of(existingProduct));
        when(userService.getCurrentSeller()).thenReturn(seller);

        // when / then
        assertThatThrownBy(() -> sellerProductService.updateProductForCurrentSeller("product-1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductForCurrentSeller_shouldValidateDatesUsingExistingProductionDateWhenOnlyBestBeforeDateIsUpdated() {
        // given
        Seller seller = Seller.builder()
                .id("seller-1")
                .build();

        Product existingProduct = createExistingProduct();

        UpdateProductRequest request = new UpdateProductRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDate.of(2026, 3, 1),
                null
        );

        when(productRepository.findById("product-1")).thenReturn(Optional.of(existingProduct));
        when(userService.getCurrentSeller()).thenReturn(seller);

        // when / then
        assertThatThrownBy(() -> sellerProductService.updateProductForCurrentSeller("product-1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductForCurrentSeller_shouldKeepExistingValuesWhenRequestFieldsAreNull() {
        // given
        Seller seller = Seller.builder()
                .id("seller-1")
                .build();

        Product existingProduct = createExistingProduct();

        UpdateProductRequest request = new UpdateProductRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(productRepository.findById("product-1")).thenReturn(Optional.of(existingProduct));
        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ProductResponse response = sellerProductService.updateProductForCurrentSeller("product-1", request);

        // then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getName()).isEqualTo("Waldhonig");
        assertThat(savedProduct.getDescription()).isEqualTo("Aromatischer Waldhonig");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo("8.99");
        assertThat(savedProduct.getCategory()).isEqualTo(ProductCategory.HONIG);
        assertThat(savedProduct.getImageId()).isEqualTo("/static/images/honey.jpg");
        assertThat(savedProduct.getProductionDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(savedProduct.getBestBeforeDate()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(savedProduct.getStockQuantity()).isEqualTo(10);

        assertThat(response.name()).isEqualTo("Waldhonig");
        assertThat(response.price()).isEqualByComparingTo("8.99");
    }

    // ------------ SOFT DELETE
    @Test
    void deactivateProductForCurrentSeller_shouldDeactivateProduct_whenProductBelongsToCurrentSeller() {
        // given
        Seller seller = Seller.builder()
                .id("seller-1")
                .build();

        Product existingProduct = createExistingProduct();

        when(productRepository.findById("product-1")).thenReturn(Optional.of(existingProduct));
        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        sellerProductService.deactivateProductForCurrentSeller("product-1");

        // then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void deactivateProductForCurrentSeller_shouldThrow_whenProductDoesNotExist() {
        when(productRepository.findById("product-1")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> sellerProductService.deactivateProductForCurrentSeller("product-1"));

        verify(productRepository, never()).save(any());
    }

    @Test
    void deactivateProductForCurrentSeller_shouldThrow_whenProductBelongsToDifferentSeller() {
        Seller currentSeller = Seller.builder()
                .id("seller-1")
                .build();

        Seller otherSeller = Seller.builder()
                .id("seller-2")
                .build();

        Product product = Product.builder()
                .id("product-1")
                .sellerId(otherSeller.getId())
                .build();

        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        assertThrows(ForbiddenAccessException.class,
                () -> sellerProductService.deactivateProductForCurrentSeller("product-1"));

        assertThat(product.getStatus()).isNotEqualTo(ProductStatus.INACTIVE);
        verify(productRepository, never()).save(any());
    }

    //------------ GET ALL PRODUCTS FROM CURRENT SELLER
    @Test
    void getCurrentSellerProducts_shouldReturnProductResponsesOfCurrentSeller() {
        Seller seller = Seller.builder()
                .id("seller-1")
                .build();

        Product product = Product.builder()
                .id("product-1")
                .sellerId("seller-1")
                .name("Pizza")
                .build();

        ProductResponse response = ProductResponse.from(product);

        Pageable pageable = PageRequest.of(0, 20);

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findBySellerId(seller.getId(), pageable)).thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = sellerProductService.getCurrentSellerProducts(pageable, null);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(response, result.getContent().getFirst());


        verify(userService).getCurrentSeller();
        verify(productRepository).findBySellerId(seller.getId(), pageable);
    }

    //------------ GET SELLER PRODUCT BY ID
    @Test
    void getSellerProductById_shouldReturnProductResponse_whenProductBelongsToCurrentSeller() {
        Seller seller = Seller.builder().id("seller-1").build();
        Product product = createExistingProduct(); // sellerId = "seller-1"

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        ProductResponse result = sellerProductService.getSellerProductById("product-1");

        assertThat(result.id()).isEqualTo("product-1");
        assertThat(result.name()).isEqualTo("Waldhonig");
        verify(userService).getCurrentSeller();
        verify(productRepository).findById("product-1");
    }

    @Test
    void getSellerProductById_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        Seller seller = Seller.builder().id("seller-1").build();

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerProductService.getSellerProductById("unknown"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Produkt nicht gefunden");
    }

    @Test
    void getSellerProductById_shouldThrowForbiddenAccessException_whenProductBelongsToDifferentSeller() {
        Seller seller = Seller.builder().id("other-seller").build();
        Product product = createExistingProduct(); // sellerId = "seller-1"

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> sellerProductService.getSellerProductById("product-1"))
                .isInstanceOf(ForbiddenAccessException.class);
    }


    //------------ UPLOAD PRODUCT IMAGE

    @Test
    void uploadProductImage_shouldSaveImageAndUpdateProductImageId() throws Exception {
        Seller seller = Seller.builder().id("seller-1").build();
        Product product = createExistingProduct(); // sellerId = "seller-1"

        MockMultipartFile file = new MockMultipartFile(
                "file", "apfel.jpg", "image/jpeg", "fake-image-bytes".getBytes()
        );

        ProductImage savedImage = ProductImage.builder()
                .id("image-1")
                .productId("product-1")
                .sellerId("seller-1")
                .filename("apfel.jpg")
                .contentType("image/jpeg")
                .size(file.getSize())
                .data(file.getBytes())
                .build();

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(savedImage);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse result = sellerProductService.uploadProductImage("product-1", file);

        verify(productImageRepository).deleteByProductId("product-1");
        ArgumentCaptor<ProductImage> imageCaptor = ArgumentCaptor.forClass(ProductImage.class);
        verify(productImageRepository).save(imageCaptor.capture());
        assertThat(imageCaptor.getValue().getContentType()).isEqualTo("image/jpeg");
        assertThat(imageCaptor.getValue().getFilename()).isEqualTo("apfel.jpg");

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getImageId()).isEqualTo("image-1");

        assertThat(result).isNotNull();
    }

    @Test
    void uploadProductImage_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        Seller seller = Seller.builder().id("seller-1").build();
        MockMultipartFile file = new MockMultipartFile(
                "file", "apfel.jpg", "image/jpeg", "bytes".getBytes()
        );

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerProductService.uploadProductImage("missing", file))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Produkt nicht gefunden");

        verifyNoInteractions(productImageRepository);
        verify(productRepository, never()).save(any());
    }

    @Test
    void uploadProductImage_shouldThrowForbiddenAccessException_whenProductBelongsToAnotherSeller() {
        Seller seller = Seller.builder().id("seller-1").build();
        Product foreignProduct = Product.builder()
                .id("product-2")
                .sellerId("seller-2")
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "file", "apfel.jpg", "image/jpeg", "bytes".getBytes()
        );

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-2")).thenReturn(Optional.of(foreignProduct));

        assertThatThrownBy(() -> sellerProductService.uploadProductImage("product-2", file))
                .isInstanceOf(ForbiddenAccessException.class);

        verifyNoInteractions(productImageRepository);
        verify(productRepository, never()).save(any());
    }

    @Test
    void uploadProductImage_shouldThrowIllegalArgumentException_whenFileIsEmpty() {
        Seller seller = Seller.builder().id("seller-1").build();
        Product product = createExistingProduct();
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> sellerProductService.uploadProductImage("product-1", emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bild darf nicht leer sein");

        verifyNoInteractions(productImageRepository);
        verify(productRepository, never()).save(any());
    }

    @Test
    void uploadProductImage_shouldThrowIllegalArgumentException_whenFileTypeIsNotAllowed() {
        Seller seller = Seller.builder().id("seller-1").build();
        Product product = createExistingProduct();
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "dokument.pdf", "application/pdf", "pdf-content".getBytes()
        );

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> sellerProductService.uploadProductImage("product-1", pdfFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nur JPEG, PNG and WEBP Formate sind erlaubt");

        verifyNoInteractions(productImageRepository);
        verify(productRepository, never()).save(any());
    }

    @Test
    void uploadProductImage_shouldThrowIllegalArgumentException_whenFileTooLarge() {
        Seller seller = Seller.builder().id("seller-1").build();
        Product product = createExistingProduct();
        byte[] oversizedContent = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", oversizedContent
        );

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> sellerProductService.uploadProductImage("product-1", largeFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bilder dürfen nicht größer als 5 MB sein");

        verifyNoInteractions(productImageRepository);
        verify(productRepository, never()).save(any());
    }


    //------------ ACTIVATE PRODUCT FOR CURRENT SELLER

    @Test
    void activateProductForCurrentSeller_shouldSetStatusToActive_whenProductIsDraft() {
        // given
        Seller seller = Seller.builder().id("seller-1").build();
        Product product = createExistingProduct(); // status = DRAFT, sellerId = "seller-1"

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        ProductResponse response = sellerProductService.activateProductForCurrentSeller("product-1");

        // then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(savedProduct.getUpdatedAt()).isNotNull();
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void activateProductForCurrentSeller_shouldSetStatusToActive_whenProductIsInactive() {
        // given
        Seller seller = Seller.builder().id("seller-1").build();
        Product inactiveProduct = Product.builder()
                .id("product-1")
                .sellerId("seller-1")
                .name("Waldhonig")
                .status(ProductStatus.INACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(inactiveProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        ProductResponse response = sellerProductService.activateProductForCurrentSeller("product-1");

        // then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void activateProductForCurrentSeller_shouldUpdateUpdatedAt() {
        // given
        Seller seller = Seller.builder().id("seller-1").build();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Product product = createExistingProduct();

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        sellerProductService.activateProductForCurrentSeller("product-1");

        // then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        assertThat(productCaptor.getValue().getUpdatedAt()).isAfter(before);
    }

    @Test
    void activateProductForCurrentSeller_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // given
        Seller seller = Seller.builder().id("seller-1").build();
        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sellerProductService.activateProductForCurrentSeller("missing"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Produkt nicht gefunden");

        verify(productRepository, never()).save(any());
    }

    @Test
    void activateProductForCurrentSeller_shouldThrowForbiddenAccessException_whenProductBelongsToAnotherSeller() {
        // given
        Seller currentSeller = Seller.builder().id("seller-1").build();
        Product foreignProduct = Product.builder()
                .id("product-1")
                .sellerId("seller-2")
                .status(ProductStatus.DRAFT)
                .build();

        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(foreignProduct));

        // when / then
        assertThatThrownBy(() -> sellerProductService.activateProductForCurrentSeller("product-1"))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage("Sie haben keine Berechtigung dieses Produktbild abzurufen");

        verify(productRepository, never()).save(any());
    }

    @Test
    void activateProductForCurrentSeller_shouldThrowIllegalStateException_whenProductIsAlreadyActive() {
        // given
        Seller seller = Seller.builder().id("seller-1").build();
        Product activeProduct = Product.builder()
                .id("product-1")
                .sellerId("seller-1")
                .status(ProductStatus.ACTIVE)
                .build();

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(activeProduct));

        // when / then
        assertThatThrownBy(() -> sellerProductService.activateProductForCurrentSeller("product-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Produkt ist bereits aktiv");

        verify(productRepository, never()).save(any());
    }

    @Test
    void activateProductForCurrentSeller_shouldThrowIllegalStateException_whenProductIsRecalled() {
        // given
        Seller seller = Seller.builder().id("seller-1").build();
        Product recalledProduct = Product.builder()
                .id("product-1")
                .sellerId("seller-1")
                .status(ProductStatus.RECALLED)
                .build();

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(recalledProduct));

        // when / then
        assertThatThrownBy(() -> sellerProductService.activateProductForCurrentSeller("product-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Produkt kann nicht aktiviert werden, da es zurückgerufen wurde");

        verify(productRepository, never()).save(any());
    }

    //------------ SLUG GENERATION

    @Test
    void createProductForCurrentSeller_shouldGenerateScopedSlug_fromShopAndProductName() {
        // given
        Seller seller = Seller.builder().id("seller-1").build();

        ShopResponse shop = new ShopResponse(
                "shop-1", "seller-1", "Honigstube Müller", "Beschreibung",
                null, null, "honigstube-muller", ShopStatus.DRAFT,
                LocalDateTime.now(), LocalDateTime.now()
        );

        CreateProductRequest request = new CreateProductRequest(
                "Waldhonig 500g",
                "Bio-Qualität",
                new BigDecimal("8.99"),
                ProductCategory.HONIG,
                null,
                null,
                null,
                5
        );

        Product savedProduct = Product.builder().id("p-1").sellerId("seller-1").shopId("shop-1")
                .name("Waldhonig 500g").slug("honigstube-muller-waldhonig-500g")
                .price(new BigDecimal("8.99")).status(ProductStatus.DRAFT).build();

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopService.getCurrentSellerShop()).thenReturn(shop);
        when(productRepository.existsByShopIdAndSlug("shop-1", "honigstube-muller-waldhonig-500g")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // when
        sellerProductService.createProductForCurrentSeller(request);

        // then
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo("honigstube-muller-waldhonig-500g");
    }

    @Test
    void createProductForCurrentSeller_shouldAppendCounter_whenSlugAlreadyExistsInShop() {
        // given
        Seller seller = Seller.builder().id("seller-1").build();

        ShopResponse shop = new ShopResponse(
                "shop-1", "seller-1", "Mein Shop", "Beschreibung",
                null, null, "mein-shop", ShopStatus.DRAFT,
                LocalDateTime.now(), LocalDateTime.now()
        );

        CreateProductRequest request = new CreateProductRequest(
                "Waldhonig",
                "Beschreibung",
                new BigDecimal("8.99"),
                ProductCategory.HONIG,
                null,
                null,
                null,
                5
        );

        Product savedProduct = Product.builder().id("p-2").sellerId("seller-1").shopId("shop-1")
                .name("Waldhonig").slug("mein-shop-waldhonig-2")
                .price(new BigDecimal("8.99")).status(ProductStatus.DRAFT).build();

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopService.getCurrentSellerShop()).thenReturn(shop);
        // base slug is taken, "-2" is free
        when(productRepository.existsByShopIdAndSlug("shop-1", "mein-shop-waldhonig")).thenReturn(true);
        when(productRepository.existsByShopIdAndSlug("shop-1", "mein-shop-waldhonig-2")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // when
        sellerProductService.createProductForCurrentSeller(request);

        // then
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo("mein-shop-waldhonig-2");
    }

    @Test
    void updateProductForCurrentSeller_shouldNotUpdateSlug_whenNameIsUnchanged() {
        // given
        Seller seller = Seller.builder().id("seller-1").build();
        Product existingProduct = createExistingProduct(); // name = "Waldhonig", slug = null

        UpdateProductRequest request = new UpdateProductRequest(
                "Waldhonig", // same name
                "Neue Beschreibung",
                null, null, null, null, null, null
        );

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        sellerProductService.updateProductForCurrentSeller("product-1", request);

        // then – shopService should NOT be called because name did not change
        verify(shopService, never()).getCurrentSellerShop();
        verify(productRepository, never()).existsByShopIdAndSlug(any(), any());
    }
}