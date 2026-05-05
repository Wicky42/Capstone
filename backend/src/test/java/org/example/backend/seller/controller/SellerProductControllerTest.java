package org.example.backend.seller.controller;

import org.example.backend.product.dto.ProductResponse;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

        Page<ProductResponse> page = new PageImpl<>(
                products,
                PageRequest.of(0, 20),
                products.size()
        );

        when(productService.getCurrentSellerProducts(any(Pageable.class), isNull()))
                .thenReturn(page);

        mockMvc.perform(get("/api/seller/products")
                        .with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value("prod-1"))
                .andExpect(jsonPath("$.content[0].name").value("Bio-Apfel"))
                .andExpect(jsonPath("$.content[1].id").value("prod-2"));

        verify(productService).getCurrentSellerProducts(any(Pageable.class), isNull());
    }

    @Test
    void getCurrentSellerProducts_returnsEmptyList_whenSellerHasNoProducts() throws Exception {
        Page<ProductResponse> emptyPage = Page.empty(PageRequest.of(0, 20));

        when(productService.getCurrentSellerProducts(any(Pageable.class), isNull()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/seller/products")
                        .with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        verify(productService).getCurrentSellerProducts(any(Pageable.class), isNull());
    }

    // ─── GET /api/seller/products/{productId} ────────────────────────────────

    @Test
    void getSellerProductById_returnsProductResponse_whenAuthenticated() throws Exception {
        ProductResponse product = buildProductResponse("prod-1");

        when(productService.getSellerProductById("prod-1")).thenReturn(product);

        mockMvc.perform(get("/api/seller/products/prod-1")
                        .with(oauth2Login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-1"))
                .andExpect(jsonPath("$.name").value("Bio-Apfel"))
                .andExpect(jsonPath("$.price").value(2.99));

        verify(productService).getSellerProductById("prod-1");
    }

    @Test
    void getSellerProductById_returns404_whenProductNotFound() throws Exception {
        when(productService.getSellerProductById("missing"))
                .thenThrow(new org.example.backend.common.exception.ProductNotFoundException());

        mockMvc.perform(get("/api/seller/products/missing")
                        .with(oauth2Login()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Produkt nicht gefunden"));

        verify(productService).getSellerProductById("missing");
    }

    @Test
    void getSellerProductById_returns403_whenProductBelongsToDifferentSeller() throws Exception {
        when(productService.getSellerProductById("prod-1"))
                .thenThrow(new org.example.backend.common.exception.ForbiddenAccessException("Zugriff verweigert: Dieses Produkt gehört nicht zu Ihrem Shop."));

        mockMvc.perform(get("/api/seller/products/prod-1")
                        .with(oauth2Login()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Zugriff verweigert: Dieses Produkt gehört nicht zu Ihrem Shop."));

        verify(productService).getSellerProductById("prod-1");
    }

    @Test
    void getSellerProductById_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/seller/products/prod-1"))
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

    // ─── POST /api/seller/products/{productId}/image ──────────────────────────

    @Test
    void uploadProductImage_returns200WithUpdatedProduct_whenFileIsValid() throws Exception {
        ProductResponse updated = buildProductResponse("prod-1");
        MockMultipartFile file = new MockMultipartFile(
                "file", "apfel.jpg", "image/jpeg", "fake-image-content".getBytes()
        );

        when(productService.uploadProductImage(eq("prod-1"), any(MultipartFile.class)))
                .thenReturn(updated);

        mockMvc.perform(multipart("/api/seller/products/prod-1/image")
                        .file(file)
                        .with(oauth2Login())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-1"))
                .andExpect(jsonPath("$.name").value("Bio-Apfel"));

        verify(productService).uploadProductImage(eq("prod-1"), any(MultipartFile.class));
    }

    @Test
    void uploadProductImage_returns400_whenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        when(productService.uploadProductImage(eq("prod-1"), any(MultipartFile.class)))
                .thenThrow(new IllegalArgumentException("Bild darf nicht leer sein"));

        mockMvc.perform(multipart("/api/seller/products/prod-1/image")
                        .file(emptyFile)
                        .with(oauth2Login())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bild darf nicht leer sein"));
    }

    @Test
    void uploadProductImage_returns400_whenFileTypeIsNotAllowed() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "dokument.pdf", "application/pdf", "pdf-content".getBytes()
        );

        when(productService.uploadProductImage(eq("prod-1"), any(MultipartFile.class)))
                .thenThrow(new IllegalArgumentException("Nur JPEG, PNG and WEBP Formate sind erlaubt"));

        mockMvc.perform(multipart("/api/seller/products/prod-1/image")
                        .file(pdfFile)
                        .with(oauth2Login())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Nur JPEG, PNG and WEBP Formate sind erlaubt"));
    }

    @Test
    void uploadProductImage_returns401_whenNotAuthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "apfel.jpg", "image/jpeg", "content".getBytes()
        );

        mockMvc.perform(multipart("/api/seller/products/prod-1/image")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    void uploadProductImage_returns403_withoutCsrfToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "apfel.jpg", "image/jpeg", "content".getBytes()
        );

        mockMvc.perform(multipart("/api/seller/products/prod-1/image")
                        .file(file)
                        .with(oauth2Login()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }
}

