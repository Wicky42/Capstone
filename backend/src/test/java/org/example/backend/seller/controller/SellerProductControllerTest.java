package org.example.backend.seller.controller;

import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SellerProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    // ─── Hilfsmethode ────────────────────────────────────────────────────────

    private ProductResponse buildProductResponse(String id) {
        return ProductResponse.builder()
                .id(id)
                .sellerId("seller-1")
                .shopId("shop-1")
                .name("Bio-Apfel")
                .description("Frische Bio-Äpfel aus der Region")
                .price(new BigDecimal("2.99"))
                .category("Obst")
                .imageUrl("apfel.png")
                .productionDate(LocalDate.of(2026, 4, 1))
                .bestBeforeDate(LocalDate.of(2026, 5, 1))
                .stockQuantity(50)
                .status(ProductStatus.DRAFT)
                .build();
    }

    private String validCreateRequestJson() {
        return """
                {
                    "name": "Bio-Apfel",
                    "description": "Frische Bio-Äpfel aus der Region",
                    "price": 2.99,
                    "category": "Obst",
                    "imageUrl": "apfel.png",
                    "productionDate": "2026-04-01",
                    "bestBeforeDate": "2026-05-01",
                    "stockQuantity": 50
                }
                """;
    }

    private String validUpdateRequestJson() {
        return """
                {
                    "name": "Bio-Birne",
                    "description": "Frische Bio-Birnen aus der Region",
                    "price": 3.49,
                    "category": "Obst",
                    "imageUrl": "birne.png",
                    "productionDate": "2026-04-10",
                    "bestBeforeDate": "2026-05-10",
                    "stockQuantity": 30
                }
                """;
    }

    // ─── GET /api/seller/products ─────────────────────────────────────────────

    @Test
    void getCurrentSellerProducts_returnsProductList_whenAuthenticated() throws Exception {
        List<ProductResponse> products = List.of(
                buildProductResponse("prod-1"),
                buildProductResponse("prod-2")
        );
        when(productService.getCurrentSellerProducts()).thenReturn(products);

        mockMvc.perform(get("/api/seller/products")
                        .with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("prod-1"))
                .andExpect(jsonPath("$[0].name").value("Bio-Apfel"))
                .andExpect(jsonPath("$[1].id").value("prod-2"));

        verify(productService).getCurrentSellerProducts();
    }

    @Test
    void getCurrentSellerProducts_returnsEmptyList_whenSellerHasNoProducts() throws Exception {
        when(productService.getCurrentSellerProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/seller/products")
                        .with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCurrentSellerProducts_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/seller/products"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    // ─── POST /api/seller/products ────────────────────────────────────────────

    @Test
    void createProduct_returns201WithProductResponse_whenRequestIsValid() throws Exception {
        ProductResponse created = buildProductResponse("prod-new");
        when(productService.createProductForCurrentSeller(any())).thenReturn(created);

        mockMvc.perform(post("/api/seller/products")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("prod-new"))
                .andExpect(jsonPath("$.name").value("Bio-Apfel"))
                .andExpect(jsonPath("$.price").value(2.99))
                .andExpect(jsonPath("$.stockQuantity").value(50));

        verify(productService).createProductForCurrentSeller(any());
    }

    @Test
    void createProduct_returns400_whenNameIsBlank() throws Exception {
        String invalidJson = """
                {
                    "name": "",
                    "description": "Frische Bio-Äpfel aus der Region",
                    "price": 2.99,
                    "category": "Obst",
                    "imageUrl": "apfel.png",
                    "productionDate": "2026-04-01",
                    "bestBeforeDate": "2026-05-01",
                    "stockQuantity": 50
                }
                """;

        mockMvc.perform(post("/api/seller/products")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void createProduct_returns400_whenPriceIsMissing() throws Exception {
        String invalidJson = """
                {
                    "name": "Bio-Apfel",
                    "description": "Frische Bio-Äpfel aus der Region",
                    "category": "Obst",
                    "imageUrl": "apfel.png",
                    "productionDate": "2026-04-01",
                    "bestBeforeDate": "2026-05-01",
                    "stockQuantity": 50
                }
                """;

        mockMvc.perform(post("/api/seller/products")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void createProduct_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/seller/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    void createProduct_returns403_withoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/seller/products")
                        .with(oauth2Login())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }

    // ─── PUT /api/seller/products/{productId} ─────────────────────────────────

    @Test
    void updateProduct_returnsUpdatedProductResponse_whenRequestIsValid() throws Exception {
        ProductResponse updated = ProductResponse.builder()
                .id("prod-1")
                .sellerId("seller-1")
                .shopId("shop-1")
                .name("Bio-Birne")
                .description("Frische Bio-Birnen aus der Region")
                .price(new BigDecimal("3.49"))
                .category("Obst")
                .imageUrl("birne.png")
                .productionDate(LocalDate.of(2026, 4, 10))
                .bestBeforeDate(LocalDate.of(2026, 5, 10))
                .stockQuantity(30)
                .status(ProductStatus.ACTIVE)
                .build();

        when(productService.updateProductForCurrentSeller(eq("prod-1"), any())).thenReturn(updated);

        mockMvc.perform(put("/api/seller/products/prod-1")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-1"))
                .andExpect(jsonPath("$.name").value("Bio-Birne"))
                .andExpect(jsonPath("$.price").value(3.49))
                .andExpect(jsonPath("$.stockQuantity").value(30));

        verify(productService).updateProductForCurrentSeller(eq("prod-1"), any());
    }

    @Test
    void updateProduct_returns400_whenPriceIsNegative() throws Exception {
        String invalidJson = """
                {
                    "name": "Bio-Birne",
                    "description": "Frische Bio-Birnen aus der Region",
                    "price": -1.00,
                    "category": "Obst",
                    "imageUrl": "birne.png",
                    "stockQuantity": 10
                }
                """;

        mockMvc.perform(put("/api/seller/products/prod-1")
                        .with(oauth2Login())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void updateProduct_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/seller/products/prod-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateRequestJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    void updateProduct_returns403_withoutCsrfToken() throws Exception {
        mockMvc.perform(put("/api/seller/products/prod-1")
                        .with(oauth2Login())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateRequestJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }

    // ─── DELETE /api/seller/products/{productId} ──────────────────────────────

    @Test
    void deactivateProduct_returns204_whenAuthenticated() throws Exception {
        doNothing().when(productService).deactivateProductForCurrentSeller("prod-1");

        mockMvc.perform(delete("/api/seller/products/prod-1")
                        .with(oauth2Login())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(productService).deactivateProductForCurrentSeller("prod-1");
    }

    @Test
    void deactivateProduct_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/seller/products/prod-1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    void deactivateProduct_returns403_withoutCsrfToken() throws Exception {
        mockMvc.perform(delete("/api/seller/products/prod-1")
                        .with(oauth2Login()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }
}

