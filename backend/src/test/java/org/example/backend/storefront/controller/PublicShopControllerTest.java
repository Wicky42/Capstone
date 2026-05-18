package org.example.backend.storefront.controller;

import org.example.backend.common.exception.ShopNotFoundException;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.service.PublicProductService;
import org.example.backend.shop.dto.ShopResponse;
import org.example.backend.shop.model.ShopStatus;
import org.example.backend.shop.service.ShopService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PublicShopControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ShopService shopService;

    @MockitoBean
    PublicProductService publicProductService;

    private ShopResponse buildActiveShop(String id, String name) {
        return new ShopResponse(
                id, "seller-1", name, "Frische Produkte aus der Region",
                null, null, "honigstube-mueller", ShopStatus.ACTIVE,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }

    private ProductResponse buildActiveProduct(String id, String name) {
        return ProductResponse.builder()
                .id(id).sellerId("seller-1").shopId("shop-1")
                .name(name).price(new BigDecimal("8.99")).category("Honig")
                .status(ProductStatus.ACTIVE)
                .productionDate(LocalDate.of(2026, 1, 1))
                .bestBeforeDate(LocalDate.of(2026, 12, 31))
                .stockQuantity(10).build();
    }

    // ─── GET /api/public/shops/{shopId} ───────────────────────────────────────

    @Test
    void getActiveShopById_returnsShop_whenShopIsActive() throws Exception {
        ShopResponse shop = buildActiveShop("shop-1", "Honigstube Müller");
        when(shopService.getActiveShopById("shop-1")).thenReturn(shop);

        mockMvc.perform(get("/api/public/shops/shop-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("shop-1"))
                .andExpect(jsonPath("$.name").value("Honigstube Müller"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.slug").value("honigstube-mueller"));

        verify(shopService).getActiveShopById("shop-1");
    }

    @Test
    void getActiveShopById_returns404_whenShopNotFound() throws Exception {
        when(shopService.getActiveShopById("missing"))
                .thenThrow(new ShopNotFoundException("Shop nicht gefunden."));

        mockMvc.perform(get("/api/public/shops/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActiveShopById_returns404_whenShopIsNotActive() throws Exception {
        when(shopService.getActiveShopById("shop-draft"))
                .thenThrow(new ShopNotFoundException("Shop ist nicht öffentlich verfügbar."));

        mockMvc.perform(get("/api/public/shops/shop-draft"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActiveShopById_isPubliclyAccessible_withoutAuthentication() throws Exception {
        ShopResponse shop = buildActiveShop("shop-1", "Honigstube Müller");
        when(shopService.getActiveShopById("shop-1")).thenReturn(shop);

        mockMvc.perform(get("/api/public/shops/shop-1"))
                .andExpect(status().isOk());
    }

    // ─── GET /api/public/shops/by-slug/{slug} ─────────────────────────────────

    @Test
    void getActiveShopBySlug_returnsShop_whenSlugExists() throws Exception {
        ShopResponse shop = buildActiveShop("shop-1", "Honigstube Müller");
        when(shopService.getActiveShopBySlug("honigstube-mueller")).thenReturn(shop);

        mockMvc.perform(get("/api/public/shops/by-slug/honigstube-mueller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("shop-1"))
                .andExpect(jsonPath("$.slug").value("honigstube-mueller"));

        verify(shopService).getActiveShopBySlug("honigstube-mueller");
    }

    @Test
    void getActiveShopBySlug_returns404_whenSlugNotFound() throws Exception {
        when(shopService.getActiveShopBySlug("unknown-slug"))
                .thenThrow(new ShopNotFoundException("Shop nicht gefunden."));

        mockMvc.perform(get("/api/public/shops/by-slug/unknown-slug"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActiveShopBySlug_returns404_whenShopIsNotActive() throws Exception {
        when(shopService.getActiveShopBySlug("draft-shop"))
                .thenThrow(new ShopNotFoundException("Shop ist nicht öffentlich verfügbar."));

        mockMvc.perform(get("/api/public/shops/by-slug/draft-shop"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActiveShopBySlug_isPubliclyAccessible_withoutAuthentication() throws Exception {
        ShopResponse shop = buildActiveShop("shop-1", "Honigstube Müller");
        when(shopService.getActiveShopBySlug("honigstube-mueller")).thenReturn(shop);

        mockMvc.perform(get("/api/public/shops/by-slug/honigstube-mueller"))
                .andExpect(status().isOk());
    }

    // ─── GET /api/public/shops/{shopId}/products ──────────────────────────────

    @Test
    void getActiveProductsByShop_returnsProducts_whenShopIsActive() throws Exception {
        var page = new PageImpl<>(List.of(
                buildActiveProduct("p-1", "Waldhonig"),
                buildActiveProduct("p-2", "Blütenhonig")
        ));
        when(publicProductService.getActiveProductsByShop(eq("shop-1"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/public/shops/shop-1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Waldhonig"));

        verify(publicProductService).getActiveProductsByShop(eq("shop-1"), any(Pageable.class));
    }

    @Test
    void getActiveProductsByShop_returns404_whenShopIsNotActive() throws Exception {
        when(publicProductService.getActiveProductsByShop(eq("shop-draft"), any(Pageable.class)))
                .thenThrow(new ShopNotFoundException("Shop ist nicht öffentlich verfügbar."));

        mockMvc.perform(get("/api/public/shops/shop-draft/products"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActiveProductsByShop_isPubliclyAccessible_withoutAuthentication() throws Exception {
        when(publicProductService.getActiveProductsByShop(eq("shop-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/public/shops/shop-1/products"))
                .andExpect(status().isOk());
    }
}