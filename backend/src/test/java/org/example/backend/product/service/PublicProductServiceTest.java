package org.example.backend.product.service;

import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.common.exception.ShopNotFoundException;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductImage;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.repository.ProductImageRepository;
import org.example.backend.product.repository.ProductRepository;
import org.example.backend.shop.service.ShopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicProductServiceTest {

    @Mock private ShopService shopService;
    @Mock private ProductRepository productRepository;
    @Mock private ProductImageRepository productImageRepository;

    @InjectMocks
    private PublicProductService publicProductService;

    private Product activeProductInActiveShop() {
        return Product.builder()
                .id("p-1")
                .shopId("shop-1")
                .name("Waldhonig")
                .status(ProductStatus.ACTIVE)
                .price(new BigDecimal("8.99"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findAllActiveProducts_returnsOnlyProductsFromActiveShops() {
        Pageable pageable = PageRequest.of(0, 20);
        List<String> activeShopIds = List.of("shop-1", "shop-2");
        Product product = activeProductInActiveShop();

        when(shopService.getActiveShopIds()).thenReturn(activeShopIds);
        when(productRepository.findByStatusAndShopIdIn(ProductStatus.ACTIVE, activeShopIds, pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = publicProductService.findAllActiveProducts(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo("p-1");
        verify(shopService).getActiveShopIds();
        verify(productRepository).findByStatusAndShopIdIn(ProductStatus.ACTIVE, activeShopIds, pageable);
    }

    @Test
    void findAllActiveProducts_returnsEmpty_whenNoActiveShops() {
        Pageable pageable = PageRequest.of(0, 20);
        when(shopService.getActiveShopIds()).thenReturn(List.of());
        when(productRepository.findByStatusAndShopIdIn(ProductStatus.ACTIVE, List.of(), pageable))
                .thenReturn(Page.empty());

        Page<ProductResponse> result = publicProductService.findAllActiveProducts(pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchActiveProducts_returnsMatchingProductsFromActiveShops() {
        Pageable pageable = PageRequest.of(0, 20);
        List<String> activeShopIds = List.of("shop-1");
        Product product = activeProductInActiveShop();

        when(shopService.getActiveShopIds()).thenReturn(activeShopIds);
        when(productRepository.findByNameContainingIgnoreCaseAndStatusAndShopIdIn(
                "honig", ProductStatus.ACTIVE, activeShopIds, pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = publicProductService.searchActiveProducts("honig", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Waldhonig");
    }

    @Test
    void searchActiveProducts_trimsQueryBeforeSearching() {
        Pageable pageable = PageRequest.of(0, 20);
        List<String> activeShopIds = List.of("shop-1");

        when(shopService.getActiveShopIds()).thenReturn(activeShopIds);
        when(productRepository.findByNameContainingIgnoreCaseAndStatusAndShopIdIn(
                "honig", ProductStatus.ACTIVE, activeShopIds, pageable))
                .thenReturn(Page.empty());

        publicProductService.searchActiveProducts("  honig  ", pageable);

        verify(productRepository).findByNameContainingIgnoreCaseAndStatusAndShopIdIn(
                "honig", ProductStatus.ACTIVE, activeShopIds, pageable);
    }

    @Test
    void getActiveProductById_returnsProduct_whenActiveAndShopActive() {
        Product product = activeProductInActiveShop();
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(shopService.isShopActive("shop-1")).thenReturn(true);

        ProductResponse result = publicProductService.getActiveProductById("p-1");

        assertThat(result.id()).isEqualTo("p-1");
    }

    @Test
    void getActiveProductById_throwsNotFound_whenProductIsInactive() {
        Product product = activeProductInActiveShop();
        product.setStatus(ProductStatus.INACTIVE);
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> publicProductService.getActiveProductById("p-1"))
                .isInstanceOf(ProductNotFoundException.class);

        verify(shopService, never()).isShopActive(any());
    }

    @Test
    void getActiveProductById_throwsNotFound_whenShopIsInactive() {
        Product product = activeProductInActiveShop();
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(shopService.isShopActive("shop-1")).thenReturn(false);

        assertThatThrownBy(() -> publicProductService.getActiveProductById("p-1"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getActiveProductById_throwsNotFound_whenProductDoesNotExist() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicProductService.getActiveProductById("missing"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getProductImage_returnsImage_whenProductAndShopActive() {
        Product product = activeProductInActiveShop();
        ProductImage image = ProductImage.builder()
                .id("img-1").productId("p-1").filename("test.jpg")
                .contentType("image/jpeg").data(new byte[0]).build();

        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(shopService.isShopActive("shop-1")).thenReturn(true);
        when(productImageRepository.findByProductId("p-1")).thenReturn(Optional.of(image));

        ProductImage result = publicProductService.getProductImage("p-1");

        assertThat(result.getId()).isEqualTo("img-1");
    }

    @Test
    void getProductImage_throwsNotFound_whenShopIsInactive() {
        Product product = activeProductInActiveShop();
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(shopService.isShopActive("shop-1")).thenReturn(false);

        assertThatThrownBy(() -> publicProductService.getProductImage("p-1"))
                .isInstanceOf(ProductNotFoundException.class);

        verifyNoInteractions(productImageRepository);
    }

    @Test
    void findAllActiveProducts_withCategory_usesCorrectRepositoryMethod() {
        Pageable pageable = PageRequest.of(0, 20);
        List<String> activeShopIds = List.of("shop-1");
        Product product = activeProductInActiveShop();

        when(shopService.getActiveShopIds()).thenReturn(activeShopIds);
        when(productRepository.findByCategoryAndStatusAndShopIdIn("Honig", ProductStatus.ACTIVE, activeShopIds, pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = publicProductService.findAllActiveProducts("Honig", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findByCategoryAndStatusAndShopIdIn("Honig", ProductStatus.ACTIVE, activeShopIds, pageable);
        verify(productRepository, never()).findByStatusAndShopIdIn(any(), any(), any());
    }

    @Test
    void findAllActiveProducts_withBlankCategory_ignoresCategory() {
        Pageable pageable = PageRequest.of(0, 20);
        List<String> activeShopIds = List.of("shop-1");

        when(shopService.getActiveShopIds()).thenReturn(activeShopIds);
        when(productRepository.findByStatusAndShopIdIn(ProductStatus.ACTIVE, activeShopIds, pageable))
                .thenReturn(Page.empty());

        publicProductService.findAllActiveProducts("   ", pageable);

        verify(productRepository).findByStatusAndShopIdIn(ProductStatus.ACTIVE, activeShopIds, pageable);
        verify(productRepository, never()).findByCategoryAndStatusAndShopIdIn(any(), any(), any(), any());
    }

    @Test
    void searchActiveProducts_withCategory_usesCorrectRepositoryMethod() {
        Pageable pageable = PageRequest.of(0, 20);
        List<String> activeShopIds = List.of("shop-1");
        Product product = activeProductInActiveShop();

        when(shopService.getActiveShopIds()).thenReturn(activeShopIds);
        when(productRepository.findByNameContainingIgnoreCaseAndCategoryAndStatusAndShopIdIn(
                "wald", "Honig", ProductStatus.ACTIVE, activeShopIds, pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = publicProductService.searchActiveProducts("wald", "Honig", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findByNameContainingIgnoreCaseAndCategoryAndStatusAndShopIdIn(
                "wald", "Honig", ProductStatus.ACTIVE, activeShopIds, pageable);
    }

    @Test
    void searchActiveProducts_withNullCategory_usesNameOnlySearch() {
        Pageable pageable = PageRequest.of(0, 20);
        List<String> activeShopIds = List.of("shop-1");

        when(shopService.getActiveShopIds()).thenReturn(activeShopIds);
        when(productRepository.findByNameContainingIgnoreCaseAndStatusAndShopIdIn(
                "honig", ProductStatus.ACTIVE, activeShopIds, pageable))
                .thenReturn(Page.empty());

        publicProductService.searchActiveProducts("honig", null, pageable);

        verify(productRepository).findByNameContainingIgnoreCaseAndStatusAndShopIdIn(
                "honig", ProductStatus.ACTIVE, activeShopIds, pageable);
        verify(productRepository, never()).findByNameContainingIgnoreCaseAndCategoryAndStatusAndShopIdIn(
                any(), any(), any(), any(), any());
    }

    @Test
    void getActiveProductsByShop_returnsProducts_whenShopIsActive() {
        Pageable pageable = PageRequest.of(0, 20);
        Product product = activeProductInActiveShop();

        when(shopService.isShopActive("shop-1")).thenReturn(true);
        when(productRepository.findByShopIdAndStatus("shop-1", ProductStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponse> result = publicProductService.getActiveProductsByShop("shop-1", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo("p-1");
        verify(productRepository).findByShopIdAndStatus("shop-1", ProductStatus.ACTIVE, pageable);
    }

    @Test
    void getActiveProductsByShop_throwsShopNotFoundException_whenShopIsInactive() {
        Pageable pageable = PageRequest.of(0, 20);

        when(shopService.isShopActive("shop-draft")).thenReturn(false);

        assertThatThrownBy(() -> publicProductService.getActiveProductsByShop("shop-draft", pageable))
                .isInstanceOf(ShopNotFoundException.class);

        verifyNoInteractions(productRepository);
    }
}
