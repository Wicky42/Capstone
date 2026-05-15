package org.example.backend.order.controller;

import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.order.dto.CheckoutResponse;
import org.example.backend.order.model.FulfillmentOrderStatus;
import org.example.backend.order.service.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CheckoutControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CheckoutService checkoutService;

    // ─── Konstanten ──────────────────────────────────────────────────────────

    private static final String ENDPOINT = "/api/orders/checkout";

    /** Gültiger Request-Body als JSON-String */
    private static final String VALID_BODY = """
            {
              "items": [
                {
                  "productId": "product-1",
                  "productName": "Waldhonig 500g",
                  "unitPrice": 8.99,
                  "quantity": 2,
                  "titleImage": "/images/honey.jpg",
                  "shopId": "shop-1",
                  "sellerId": "seller-1"
                }
              ],
              "shippingAddress": {
                "street": "Musterstrasse",
                "houseNumber": "12a",
                "postalCode": "12345",
                "city": "Berlin",
                "country": "Deutschland"
              },
              "billingAddress": {
                "street": "Musterstrasse",
                "houseNumber": "12a",
                "postalCode": "12345",
                "city": "Berlin",
                "country": "Deutschland"
              }
            }
            """;

    private CheckoutResponse successResponse;

    @BeforeEach
    void setUp() {
        successResponse = new CheckoutResponse("fo-1", "inv-1", FulfillmentOrderStatus.CREATED, 17.98);
    }

    // ═══════════════════ Authorisierungs-Tests ═══════════════════════════════

    /**
     * CUSTOMER ist eingeloggt → Service wird aufgerufen → 201 Created mit Body.
     */
    @Test
    void checkout_returns201WithBody_whenCustomerIsAuthenticated() throws Exception {
        when(checkoutService.checkout(any())).thenReturn(successResponse);

        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("fo-1"))
                .andExpect(jsonPath("$.invoiceId").value("inv-1"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalPrice").value(17.98));

        verify(checkoutService).checkout(any());
    }

    /**
     * Kein Login → @PreAuthorize greift → 401 Unauthorized.
     */
    @Test
    void checkout_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(checkoutService);
    }

    /**
     * SELLER hat nicht die Rolle CUSTOMER → @PreAuthorize → 403 Forbidden.
     */
    @Test
    void checkout_returns403_whenSellerTriesToCheckout() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(checkoutService);
    }

    /**
     * ADMIN hat nicht die Rolle CUSTOMER → @PreAuthorize → 403 Forbidden.
     */
    @Test
    void checkout_returns403_whenAdminTriesToCheckout() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(checkoutService);
    }

    // ═══════════════════ Validierungs-Tests (400 Bad Request) ════════════════

    /**
     * items = null verletzt @NotNull → 400 Bad Request.
     */
    @Test
    void checkout_returns400_whenItemsIsNull() throws Exception {
        String body = """
                {
                  "items": null,
                  "shippingAddress": { "street": "A", "houseNumber": "1", "postalCode": "12345", "city": "B", "country": "C" },
                  "billingAddress":  { "street": "A", "houseNumber": "1", "postalCode": "12345", "city": "B", "country": "C" }
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(checkoutService);
    }

    /**
     * Leeres Array verletzt @NotEmpty → 400 Bad Request.
     */
    @Test
    void checkout_returns400_whenItemsIsEmpty() throws Exception {
        String body = """
                {
                  "items": [],
                  "shippingAddress": { "street": "A", "houseNumber": "1", "postalCode": "12345", "city": "B", "country": "C" },
                  "billingAddress":  { "street": "A", "houseNumber": "1", "postalCode": "12345", "city": "B", "country": "C" }
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(checkoutService);
    }

    /**
     * shippingAddress = null verletzt @NotNull → 400 Bad Request.
     */
    @Test
    void checkout_returns400_whenShippingAddressIsNull() throws Exception {
        String body = """
                {
                  "items": [{ "productId": "p-1", "quantity": 1, "unitPrice": 5.0 }],
                  "shippingAddress": null,
                  "billingAddress":  { "street": "A", "houseNumber": "1", "postalCode": "12345", "city": "B", "country": "C" }
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(checkoutService);
    }

    /**
     * billingAddress = null verletzt @NotNull → 400 Bad Request.
     */
    @Test
    void checkout_returns400_whenBillingAddressIsNull() throws Exception {
        String body = """
                {
                  "items": [{ "productId": "p-1", "quantity": 1, "unitPrice": 5.0 }],
                  "shippingAddress": { "street": "A", "houseNumber": "1", "postalCode": "12345", "city": "B", "country": "C" },
                  "billingAddress": null
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(checkoutService);
    }

    // ═══════════════════ Service-Fehler ══════════════════════════════════════

    /**
     * Service wirft IllegalArgumentException (z. B. leerer Warenkorb nach Bereinigung)
     * → GlobalExceptionHandler → 400 Bad Request.
     */
    @Test
    void checkout_returns400_whenServiceThrowsIllegalArgumentException() throws Exception {
        when(checkoutService.checkout(any())).thenThrow(
                new IllegalArgumentException("Der Warenkorb darf nicht leer sein.")
        );

        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Der Warenkorb darf nicht leer sein."));
    }

    /**
     * Service wirft ProductNotFoundException (Produkt nicht in DB)
     * → GlobalExceptionHandler → 404 Not Found.
     */
    @Test
    void checkout_returns404_whenServiceThrowsProductNotFoundException() throws Exception {
        when(checkoutService.checkout(any())).thenThrow(new ProductNotFoundException());

        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Produkt nicht gefunden"));
    }

    /**
     * Service wirft IllegalStateException (Produkt nicht mehr ACTIVE)
     * → GlobalExceptionHandler → 422 Unprocessable Entity.
     */
    @Test
    void checkout_returns422_whenServiceThrowsIllegalStateException() throws Exception {
        when(checkoutService.checkout(any())).thenThrow(
                new IllegalStateException("Produkt 'Waldhonig 500g' ist nicht mehr verfügbar.")
        );

        mockMvc.perform(post(ENDPOINT)
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Produkt 'Waldhonig 500g' ist nicht mehr verfügbar."));
    }

    // ═══════════════════ CSRF-Schutz ═════════════════════════════════════════

    /**
     * Kein CSRF-Token → Spring Security CSRF-Filter → 403 Forbidden.
     */
    @Test
    void checkout_returns403_whenCsrfTokenIsMissing() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(checkoutService);
    }
}

