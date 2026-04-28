package org.example.backend.product.service;

import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.product.dto.CreateProductRequest;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.dto.UpdateProductRequest;
import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @Mock
    private ShopService shopService;

    @InjectMocks
    private ProductService productService;

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
                .category("HONIG")
                .imageUrl("/static/images/honey.jpg")
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
                "HONIG",
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
                .category("HONIG")
                .imageUrl("/static/images/honey.jpg")
                .productionDate(LocalDate.of(2026, 4, 1))
                .bestBeforeDate(LocalDate.of(2027, 4, 1))
                .stockQuantity(12)
                .status(ProductStatus.DRAFT)
                .build();

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(shopService.getCurrentSellerShop()).thenReturn(shop);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // when
        ProductResponse response = productService.createProductForCurrentSeller(request);

        // then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product productToSave = productCaptor.getValue();

        assertThat(productToSave.getSellerId()).isEqualTo("seller-1");
        assertThat(productToSave.getShopId()).isEqualTo("shop-1");
        assertThat(productToSave.getName()).isEqualTo("Waldhonig");
        assertThat(productToSave.getDescription()).isEqualTo("Aromatischer Waldhonig aus regionaler Imkerei");
        assertThat(productToSave.getPrice()).isEqualByComparingTo("9.00");
        assertThat(productToSave.getCategory()).isEqualTo("HONIG");
        assertThat(productToSave.getImageUrl()).isEqualTo("/static/images/honey.jpg");
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
                "HONIG",
                "/static/images/honey.jpg",
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 1),
                12
        );

        // when / then
        assertThatThrownBy(() -> productService.createProductForCurrentSeller(request))
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
                "HONIG",
                "/static/images/honey.jpg",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 4, 1),
                -1
        );

        // when / then
        assertThatThrownBy(() -> productService.createProductForCurrentSeller(request))
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
                "HONIG",
                "/static/images/honey.jpg",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 4, 1),
                12
        );

        // when / then
        assertThatThrownBy(() -> productService.createProductForCurrentSeller(request))
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

        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 10, 0);

        Product existingProduct = Product.builder()
                .id("product-1")
                .sellerId("seller-1")
                .shopId("shop-1")
                .name("Alter Honig")
                .description("Alte Beschreibung")
                .price(new BigDecimal("8.99"))
                .category("HONIG")
                .imageUrl("/static/images/old.jpg")
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
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ProductResponse response = productService.updateProductForCurrentSeller("product-1", request);

        // then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getId()).isEqualTo("product-1");
        assertThat(savedProduct.getSellerId()).isEqualTo("seller-1");
        assertThat(savedProduct.getShopId()).isEqualTo("shop-1");

        assertThat(savedProduct.getName()).isEqualTo("Neuer Waldhonig");
        assertThat(savedProduct.getDescription()).isEqualTo("Alte Beschreibung");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo("10.00");
        assertThat(savedProduct.getCategory()).isEqualTo("HONIG");
        assertThat(savedProduct.getImageUrl()).isEqualTo("/static/images/new.jpg");
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
        assertThatThrownBy(() -> productService.updateProductForCurrentSeller("missing-product", request))
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
                .category("HONIG")
                .imageUrl("/static/images/honey.jpg")
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
        assertThatThrownBy(() -> productService.updateProductForCurrentSeller("product-1", request))
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
        assertThatThrownBy(() -> productService.updateProductForCurrentSeller("product-1", request))
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
        assertThatThrownBy(() -> productService.updateProductForCurrentSeller("product-1", request))
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
        assertThatThrownBy(() -> productService.updateProductForCurrentSeller("product-1", request))
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
        assertThatThrownBy(() -> productService.updateProductForCurrentSeller("product-1", request))
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
        ProductResponse response = productService.updateProductForCurrentSeller("product-1", request);

        // then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getName()).isEqualTo("Waldhonig");
        assertThat(savedProduct.getDescription()).isEqualTo("Aromatischer Waldhonig");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo("8.99");
        assertThat(savedProduct.getCategory()).isEqualTo("HONIG");
        assertThat(savedProduct.getImageUrl()).isEqualTo("/static/images/honey.jpg");
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
        productService.deactivateProductForCurrentSeller("product-1");

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
                () -> productService.deactivateProductForCurrentSeller("product-1"));

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
                .sellerId("seller-2")
                .build();

        when(userService.getCurrentSeller()).thenReturn(currentSeller);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        assertThrows(ForbiddenAccessException.class,
                () -> productService.deactivateProductForCurrentSeller("product-1"));

        assertFalse(product.getStatus() == ProductStatus.INACTIVE);
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

        when(userService.getCurrentSeller()).thenReturn(seller);
        when(productRepository.findBySellerId(seller.getId())).thenReturn(List.of(product));

        List<ProductResponse> result = productService.getCurrentSellerProducts();

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));

        verify(productRepository).findBySellerId(seller.getId());
    }

    //------------ GET PRODUCT BY ID
    @Test
    void getProductById_shouldReturnProductResponse_whenProductExists() {
        Product product = createExistingProduct();
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProductById("product-1");

        assertThat(result.id()).isEqualTo("product-1");
        assertThat(result.name()).isEqualTo("Waldhonig");
        verify(productRepository).findById("product-1");
    }

    @Test
    void getProductById_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById("missing"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Produkt nicht gefunden");
    }

    //------------ GET ACTIVE PRODUCT BY ID
    @Test
    void getActiveProductById_shouldReturnResponse_whenProductIsActive() {
        Product product = Product.builder()
                .id("product-1")
                .sellerId("seller-1")
                .name("Waldhonig")
                .status(ProductStatus.ACTIVE)
                .build();
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        ProductResponse result = productService.getActiveProductById("product-1");

        assertThat(result.status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void getActiveProductById_shouldThrowProductNotFoundException_whenProductIsDraft() {
        Product product = createExistingProduct(); // status = DRAFT
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.getActiveProductById("product-1"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Produkt nicht gefunden");
    }

    @Test
    void getActiveProductById_shouldThrowProductNotFoundException_whenProductIsInactive() {
        Product product = Product.builder()
                .id("product-1")
                .sellerId("seller-1")
                .name("Waldhonig")
                .status(ProductStatus.INACTIVE)
                .build();
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.getActiveProductById("product-1"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    //------------ SEARCH PRODUCTS
    @Test
    void searchProducts_shouldUseQueryAndSellerIdFilter_whenBothProvided() {
        Product product = createExistingProduct();
        when(productRepository.findByNameContainingIgnoreCaseAndSellerIdAndStatus(
                "honig", "seller-1", ProductStatus.ACTIVE))
                .thenReturn(List.of(product));

        List<ProductResponse> result = productService.searchProducts("honig", "seller-1", true);

        assertThat(result.size()).isEqualTo(1);
        verify(productRepository).findByNameContainingIgnoreCaseAndSellerIdAndStatus(
                "honig", "seller-1", ProductStatus.ACTIVE);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void searchProducts_shouldUseQueryOnlyFilter_whenOnlyQueryProvided() {
        Product product = createExistingProduct();
        when(productRepository.findByNameContainingIgnoreCaseAndStatus("honig", ProductStatus.ACTIVE))
                .thenReturn(List.of(product));

        List<ProductResponse> result = productService.searchProducts("honig", null, true);

        assertThat(result.size()).isEqualTo(1);
        verify(productRepository).findByNameContainingIgnoreCaseAndStatus("honig", ProductStatus.ACTIVE);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void searchProducts_shouldUseSellerIdOnlyFilter_whenOnlySellerIdProvided() {
        Product product = createExistingProduct();
        when(productRepository.findBySellerIdAndStatus("seller-1", ProductStatus.ACTIVE))
                .thenReturn(List.of(product));

        List<ProductResponse> result = productService.searchProducts(null, "seller-1", true);

        assertThat(result.size()).isEqualTo(1);
        verify(productRepository).findBySellerIdAndStatus("seller-1", ProductStatus.ACTIVE);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void searchProducts_shouldReturnAllActiveProducts_whenNoFilterProvided() {
        Product product = createExistingProduct();
        when(productRepository.findByStatus(ProductStatus.ACTIVE)).thenReturn(List.of(product));

        List<ProductResponse> result = productService.searchProducts(null, null, true);

        assertThat(result.size()).isEqualTo(1);
        verify(productRepository).findByStatus(ProductStatus.ACTIVE);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void searchProducts_shouldUseInactiveStatus_whenActiveFlagIsFalse() {
        when(productRepository.findByStatus(ProductStatus.INACTIVE)).thenReturn(List.of());

        productService.searchProducts(null, null, false);

        verify(productRepository).findByStatus(ProductStatus.INACTIVE);
    }

    @Test
    void searchProducts_shouldIgnoreBlankQueryAndUseSellerIdOnly_whenQueryIsBlank() {
        Product product = createExistingProduct();
        when(productRepository.findBySellerIdAndStatus("seller-1", ProductStatus.ACTIVE))
                .thenReturn(List.of(product));

        List<ProductResponse> result = productService.searchProducts("   ", "seller-1", true);

        assertThat(result.size()).isEqualTo(1);
        verify(productRepository).findBySellerIdAndStatus("seller-1", ProductStatus.ACTIVE);
    }
}