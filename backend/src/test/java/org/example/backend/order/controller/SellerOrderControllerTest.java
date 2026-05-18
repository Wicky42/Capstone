package org.example.backend.order.controller;

import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.common.exception.OrderNotFoundException;
import org.example.backend.order.dto.SellerOrderResponse;
import org.example.backend.order.model.SellerOrderStatus;
import org.example.backend.order.service.SellerOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SellerOrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SellerOrderService sellerOrderService;

    private static final String BASE_URL = "/api/seller/orders";

    private SellerOrderResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new SellerOrderResponse("order-1", null, null, null, null, SellerOrderStatus.CREATED, null, null);
    }

    // ═══════════════════ GET /api/seller/orders ═══════════════════════════════

    @Test
    void getAllSellerOrders_returns200WithList_whenSellerIsAuthenticated() throws Exception {
        when(sellerOrderService.getAllOrdersForCurrentSeller()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get(BASE_URL)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("order-1"))
                .andExpect(jsonPath("$[0].status").value("CREATED"));

        verify(sellerOrderService).getAllOrdersForCurrentSeller();
    }

    @Test
    void getAllSellerOrders_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sellerOrderService);
    }

    @Test
    void getAllSellerOrders_returns403_whenCustomerTriesToAccess() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(sellerOrderService);
    }

    @Test
    void getAllSellerOrders_returns403_whenAdminTriesToAccess() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(sellerOrderService);
    }

    @Test
    void getAllSellerOrders_returnsEmptyList_whenSellerHasNoOrders() throws Exception {
        when(sellerOrderService.getAllOrdersForCurrentSeller()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ═══════════════════ GET /api/seller/orders/{id} ══════════════════════════

    @Test
    void getSellerOrderById_returns200WithOrder_whenSellerOwnsOrder() throws Exception {
        when(sellerOrderService.getSellerOrderById("order-1")).thenReturn(sampleResponse);

        mockMvc.perform(get(BASE_URL + "/order-1")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-1"))
                .andExpect(jsonPath("$.status").value("CREATED"));

        verify(sellerOrderService).getSellerOrderById("order-1");
    }

    @Test
    void getSellerOrderById_returns404_whenOrderDoesNotExist() throws Exception {
        when(sellerOrderService.getSellerOrderById("unknown"))
                .thenThrow(new OrderNotFoundException("SellerOrder mit ID unknown nicht gefunden"));

        mockMvc.perform(get(BASE_URL + "/unknown")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("SellerOrder mit ID unknown nicht gefunden"));
    }

    @Test
    void getSellerOrderById_returns403_whenOrderBelongsToOtherSeller() throws Exception {
        when(sellerOrderService.getSellerOrderById("order-2"))
                .thenThrow(new ForbiddenAccessException("Kein Zugriff auf diese Bestellung"));

        mockMvc.perform(get(BASE_URL + "/order-2")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Kein Zugriff auf diese Bestellung"));
    }

    @Test
    void getSellerOrderById_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/order-1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sellerOrderService);
    }

    // ═══════════════════ PATCH /api/seller/orders/{id}/status ═════════════════

    @Test
    void updateSellerOrderStatus_returns200WithUpdatedOrder_whenTransitionIsValid() throws Exception {
        SellerOrderResponse updatedResponse = new SellerOrderResponse(
                "order-1", null, null, null, null, SellerOrderStatus.CONFIRMED, null, null);

        when(sellerOrderService.updateSellerOrderStatus("order-1", SellerOrderStatus.CONFIRMED))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch(BASE_URL + "/order-1/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONFIRMED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(sellerOrderService).updateSellerOrderStatus("order-1", SellerOrderStatus.CONFIRMED);
    }

    @Test
    void updateSellerOrderStatus_returns400_whenTransitionIsInvalid() throws Exception {
        when(sellerOrderService.updateSellerOrderStatus("order-1", SellerOrderStatus.IN_PREPARATION))
                .thenThrow(new IllegalArgumentException("Ungültiger Statusübergang von CREATED nach IN_PREPARATION"));

        mockMvc.perform(patch(BASE_URL + "/order-1/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"IN_PREPARATION"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ungültiger Statusübergang von CREATED nach IN_PREPARATION"));
    }

    @Test
    void updateSellerOrderStatus_returns403_whenOrderBelongsToOtherSeller() throws Exception {
        when(sellerOrderService.updateSellerOrderStatus("order-2", SellerOrderStatus.CONFIRMED))
                .thenThrow(new ForbiddenAccessException("Kein Zugriff auf diese Bestellung"));

        mockMvc.perform(patch(BASE_URL + "/order-2/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONFIRMED"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSellerOrderStatus_returns404_whenOrderDoesNotExist() throws Exception {
        when(sellerOrderService.updateSellerOrderStatus("unknown", SellerOrderStatus.CONFIRMED))
                .thenThrow(new OrderNotFoundException("SellerOrder mit ID unknown nicht gefunden"));

        mockMvc.perform(patch(BASE_URL + "/unknown/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONFIRMED"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSellerOrderStatus_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/order-1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONFIRMED"}
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sellerOrderService);
    }

    @Test
    void updateSellerOrderStatus_returns403_whenCustomerTriesToUpdate() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/order-1/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONFIRMED"}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(sellerOrderService);
    }

    @Test
    void updateSellerOrderStatus_returns403_whenCsrfTokenIsMissing() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/order-1/status")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONFIRMED"}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(sellerOrderService);
    }
}

