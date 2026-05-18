package org.example.backend.storefront.controller;

import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.storefront.service.StorefrontService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StorefrontControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StorefrontService storefrontService;

    private ProductResponse buildProduct(String id, String name) {
        return ProductResponse.builder()
                .id(id).sellerId("seller-1").shopId("shop-1")
                .name(name).price(new BigDecimal("5.99")).category("Honig")
                .status(ProductStatus.ACTIVE)
                .productionDate(LocalDate.of(2026, 1, 1))
                .bestBeforeDate(LocalDate.of(2026, 12, 31))
                .stockQuantity(10).build();
    }

    private ShopResponse buildShop(String id, String name) {
        return new ShopResponse(id, "seller-1", name, "Beschreibung",
                null, null, "slug", ShopStatus.ACTIVE,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    // ─── GET /api/public/storefront/products ──────────────────────────────────

    @Test
    void getProductView_returnsAllProducts_whenNoQueryAndNoCategory() throws Exception {
        var page = new PageImpl<>(List.of(buildProduct("p-1", "Waldhonig")));
        when(storefrontService.getProductView(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/public/storefront/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Waldhonig"));

        verify(storefrontService).getProductView(isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getProductView_delegatesToService_whenQueryIsPresent() throws Exception {
        var page = new PageImpl<>(List.of(buildProduct("p-1", "Waldhonig")));
        when(storefrontService.getProductView(eq("honig"), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/public/storefront/products").param("query", "honig"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Waldhonig"));

        verify(storefrontService).getProductView(eq("honig"), isNull(), any(Pageable.class));
    }

    @Test
    void getProductView_filtersByCategory_whenCategoryIsPresent() throws Exception {
        var page = new PageImpl<>(List.of(buildProduct("p-1", "Waldhonig")));
        when(storefrontService.getProductView(isNull(), eq("Honig"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/public/storefront/products").param("category", "Honig"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(storefrontService).getProductView(isNull(), eq("Honig"), any(Pageable.class));
    }

    @Test
    void getProductView_combinesQueryAndCategory() throws Exception {
        var page = new PageImpl<>(List.of(buildProduct("p-1", "Waldhonig")));
        when(storefrontService.getProductView(eq("wald"), eq("Honig"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/public/storefront/products")
                        .param("query", "wald")
                        .param("category", "Honig"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("p-1"));

        verify(storefrontService).getProductView(eq("wald"), eq("Honig"), any(Pageable.class));
    }

    @Test
    void getProductView_returnsEmptyPage_whenNoMatches() throws Exception {
        when(storefrontService.getProductView(eq("xyz"), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/public/storefront/products").param("query", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getProductView_isPubliclyAccessible_withoutAuthentication() throws Exception {
        when(storefrontService.getProductView(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/public/storefront/products"))
                .andExpect(status().isOk());
    }

    // ─── GET /api/public/storefront/shops ─────────────────────────────────────

    @Test
    void getShopView_returnsAllActiveShops() throws Exception {
        var page = new PageImpl<>(List.of(
                buildShop("shop-1", "Honigstube Müller"),
                buildShop("shop-2", "Bauernhof Schmitt")
        ));
        when(storefrontService.getShopView(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/public/storefront/shops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Honigstube Müller"));

        verify(storefrontService).getShopView(any(Pageable.class));
    }

    @Test
    void getShopView_returnsEmptyPage_whenNoActiveShops() throws Exception {
        when(storefrontService.getShopView(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/public/storefront/shops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getShopView_isPubliclyAccessible_withoutAuthentication() throws Exception {
        when(storefrontService.getShopView(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/public/storefront/shops"))
                .andExpect(status().isOk());
    }
}

