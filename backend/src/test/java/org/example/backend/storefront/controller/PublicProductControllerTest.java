package org.example.backend.storefront.controller;

import org.example.backend.common.exception.ProductImageNotFoundException;
import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.ProductImage;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.service.PublicProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PublicProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PublicProductService publicProductService;

    private ProductResponse buildActiveProduct(String id, String name) {
        return ProductResponse.builder()
                .id(id)
                .sellerId("seller-1")
                .shopId("shop-1")
                .name(name)
                .description("Frisches Produkt aus der Region")
                .price(new BigDecimal("3.99"))
                .category("Obst")
                .imageUrl("produkt.png")
                .productionDate(LocalDate.of(2026, 3, 1))
                .bestBeforeDate(LocalDate.of(2026, 6, 1))
                .stockQuantity(20)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    // ─── GET /api/public/products ─────────────────────────────────────────────

    @Test
    void searchActiveProducts_returnsAllActiveProducts_whenNoQueryGiven() throws Exception {
        var page = new PageImpl<>(List.of(
                buildActiveProduct("prod-1", "Bio-Apfel"),
                buildActiveProduct("prod-2", "Bio-Birne")
        ));
        when(publicProductService.findAllActiveProducts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value("prod-1"))
                .andExpect(jsonPath("$.content[0].name").value("Bio-Apfel"))
                .andExpect(jsonPath("$.content[1].id").value("prod-2"))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(publicProductService).findAllActiveProducts(any(Pageable.class));
        verify(publicProductService, never()).searchActiveProducts(any(), any());
    }

    @Test
    void searchActiveProducts_delegatesToSearch_whenQueryIsPresent() throws Exception {
        var page = new PageImpl<>(List.of(buildActiveProduct("prod-1", "Bio-Apfel")));
        when(publicProductService.searchActiveProducts(eq("apfel"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/public/products").param("query", "apfel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Bio-Apfel"));

        verify(publicProductService).searchActiveProducts(eq("apfel"), any(Pageable.class));
        verify(publicProductService, never()).findAllActiveProducts(any());
    }

    @Test
    void searchActiveProducts_delegatesToFindAll_whenQueryIsBlank() throws Exception {
        when(publicProductService.findAllActiveProducts(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/public/products").param("query", "   "))
                .andExpect(status().isOk());

        verify(publicProductService).findAllActiveProducts(any(Pageable.class));
        verify(publicProductService, never()).searchActiveProducts(any(), any());
    }

    @Test
    void searchActiveProducts_returnsEmptyPage_whenNoProductsMatch() throws Exception {
        when(publicProductService.searchActiveProducts(eq("nichtvorhanden"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/public/products").param("query", "nichtvorhanden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void searchActiveProducts_respectsPageableParams() throws Exception {
        when(publicProductService.findAllActiveProducts(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildActiveProduct("prod-1", "Bio-Apfel"))));

        mockMvc.perform(get("/api/public/products").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void searchActiveProducts_isPubliclyAccessible_withoutAuthentication() throws Exception {
        when(publicProductService.findAllActiveProducts(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk());
    }

    // ─── GET /api/public/products/{productId} ─────────────────────────────────

    @Test
    void getActiveProductById_returnsProduct_whenProductExists() throws Exception {
        ProductResponse product = buildActiveProduct("prod-1", "Bio-Apfel");
        when(publicProductService.getActiveProductById("prod-1")).thenReturn(product);

        mockMvc.perform(get("/api/public/products/prod-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-1"))
                .andExpect(jsonPath("$.name").value("Bio-Apfel"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.price").value(3.99));

        verify(publicProductService).getActiveProductById("prod-1");
    }

    @Test
    void getActiveProductById_returns404_whenProductNotFound() throws Exception {
        when(publicProductService.getActiveProductById("missing"))
                .thenThrow(new ProductNotFoundException());

        mockMvc.perform(get("/api/public/products/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActiveProductById_isPubliclyAccessible_withoutAuthentication() throws Exception {
        ProductResponse product = buildActiveProduct("prod-1", "Bio-Apfel");
        when(publicProductService.getActiveProductById("prod-1")).thenReturn(product);

        mockMvc.perform(get("/api/public/products/prod-1"))
                .andExpect(status().isOk());
    }

    // ─── GET /api/public/products/{productId}/image ───────────────────────────

    @Test
    void getProductImage_returnsImageBytes_withCorrectContentType_whenImageExists() throws Exception {
        byte[] imageBytes = "fake-jpeg-content".getBytes();
        ProductImage image = ProductImage.builder()
                .id("image-1").productId("prod-1").filename("apfel.jpg")
                .contentType("image/jpeg").data(imageBytes).build();

        when(publicProductService.getProductImage("prod-1")).thenReturn(image);

        mockMvc.perform(get("/api/public/products/prod-1/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(imageBytes));

        verify(publicProductService).getProductImage("prod-1");
    }

    @Test
    void getProductImage_returnsPngBytes_withCorrectContentType() throws Exception {
        byte[] imageBytes = "fake-png-content".getBytes();
        ProductImage image = ProductImage.builder()
                .id("image-2").productId("prod-2").filename("logo.png")
                .contentType("image/png").data(imageBytes).build();

        when(publicProductService.getProductImage("prod-2")).thenReturn(image);

        mockMvc.perform(get("/api/public/products/prod-2/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));
    }

    @Test
    void getProductImage_returns404_whenProductNotFound() throws Exception {
        when(publicProductService.getProductImage("missing"))
                .thenThrow(new ProductNotFoundException());

        mockMvc.perform(get("/api/public/products/missing/image"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductImage_returns404_whenImageNotFound() throws Exception {
        when(publicProductService.getProductImage("prod-1"))
                .thenThrow(new ProductImageNotFoundException("Produktbild nicht gefunden"));

        mockMvc.perform(get("/api/public/products/prod-1/image"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductImage_isPubliclyAccessible_withoutAuthentication() throws Exception {
        byte[] imageBytes = "fake-jpeg-content".getBytes();
        ProductImage image = ProductImage.builder()
                .id("image-1").productId("prod-1").filename("apfel.jpg")
                .contentType("image/jpeg").data(imageBytes).build();

        when(publicProductService.getProductImage("prod-1")).thenReturn(image);

        mockMvc.perform(get("/api/public/products/prod-1/image"))
                .andExpect(status().isOk());
    }
}
