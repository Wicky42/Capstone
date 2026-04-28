package org.example.backend.product.service;

import org.example.backend.product.dto.CreateProductRequest;
import org.example.backend.product.dto.ProductResponse;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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



}