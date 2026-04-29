package org.example.backend.product.controller;

import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.service.ProductService;
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
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    // ─── Hilfsmethode ────────────────────────────────────────────────────────

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

    // ─── GET /api/products ────────────────────────────────────────────────────

    @Test
    void searchActiveProducts_returnsProductPage_withNoParams() throws Exception {
        var page = new PageImpl<>(List.of(
                buildActiveProduct("prod-1", "Bio-Apfel"),
                buildActiveProduct("prod-2", "Bio-Birne")
        ));
        when(productService.searchProducts(eq(null), eq(null), eq(true), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value("prod-1"))
                .andExpect(jsonPath("$.content[0].name").value("Bio-Apfel"))
                .andExpect(jsonPath("$.content[1].id").value("prod-2"))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(productService).searchProducts(eq(null), eq(null), eq(true), any(Pageable.class));
    }

    @Test
    void searchActiveProducts_returnsFilteredPage_withQueryParam() throws Exception {
        var page = new PageImpl<>(List.of(buildActiveProduct("prod-1", "Bio-Apfel")));
        when(productService.searchProducts(eq("apfel"), eq(null), eq(true), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products").param("query", "apfel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Bio-Apfel"));

        verify(productService).searchProducts(eq("apfel"), eq(null), eq(true), any(Pageable.class));
    }

    @Test
    void searchActiveProducts_returnsFilteredPage_withSellerIdParam() throws Exception {
        var page = new PageImpl<>(List.of(buildActiveProduct("prod-1", "Bio-Apfel")));
        when(productService.searchProducts(eq(null), eq("seller-1"), eq(true), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products").param("sellerId", "seller-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].sellerId").value("seller-1"));

        verify(productService).searchProducts(eq(null), eq("seller-1"), eq(true), any(Pageable.class));
    }

    @Test
    void searchActiveProducts_returnsFilteredPage_withQueryAndSellerIdParam() throws Exception {
        var page = new PageImpl<>(List.of(buildActiveProduct("prod-1", "Bio-Apfel")));
        when(productService.searchProducts(eq("apfel"), eq("seller-1"), eq(true), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("query", "apfel")
                        .param("sellerId", "seller-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(productService).searchProducts(eq("apfel"), eq("seller-1"), eq(true), any(Pageable.class));
    }

    @Test
    void searchActiveProducts_returnsEmptyPage_whenNoProductsMatch() throws Exception {
        when(productService.searchProducts(any(), any(), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/products").param("query", "nichtvorhanden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void searchActiveProducts_respectsPageSizeAndPageNumber() throws Exception {
        var page = new PageImpl<>(List.of(buildActiveProduct("prod-1", "Bio-Apfel")));
        when(productService.searchProducts(eq(null), eq(null), eq(true), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void searchActiveProducts_isPubliclyAccessible_withoutAuthentication() throws Exception {
        when(productService.searchProducts(eq(null), eq(null), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    // ─── GET /api/products/{productId} ───────────────────────────────────────

    @Test
    void getActiveProductById_returnsProduct_whenProductExists() throws Exception {
        ProductResponse product = buildActiveProduct("prod-1", "Bio-Apfel");
        when(productService.getActiveProductById("prod-1")).thenReturn(product);

        mockMvc.perform(get("/api/products/prod-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-1"))
                .andExpect(jsonPath("$.name").value("Bio-Apfel"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.price").value(3.99));

        verify(productService).getActiveProductById("prod-1");
    }

    @Test
    void getActiveProductById_returns404_whenProductNotFound() throws Exception {
        when(productService.getActiveProductById("missing"))
                .thenThrow(new ProductNotFoundException("Produkt nicht gefunden"));

        mockMvc.perform(get("/api/products/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActiveProductById_isPubliclyAccessible_withoutAuthentication() throws Exception {
        ProductResponse product = buildActiveProduct("prod-1", "Bio-Apfel");
        when(productService.getActiveProductById("prod-1")).thenReturn(product);

        mockMvc.perform(get("/api/products/prod-1"))
                .andExpect(status().isOk());
    }
}

