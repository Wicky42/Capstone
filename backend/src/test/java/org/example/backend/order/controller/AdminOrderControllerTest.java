package org.example.backend.order.controller;

import org.example.backend.common.exception.OrderNotFoundException;
import org.example.backend.order.dto.AdminFulfillmentOrderDetail;
import org.example.backend.order.dto.FulfillmentOrderResponse;
import org.example.backend.order.dto.SellerOrderResponse;
import org.example.backend.order.model.FulfillmentOrderStatus;
import org.example.backend.order.model.SellerOrderStatus;
import org.example.backend.order.service.AdminOrderService;
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
class AdminOrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminOrderService adminOrderService;

    private static final String BASE_URL = "/api/admin/orders";

    private FulfillmentOrderResponse sampleResponse;
    private AdminFulfillmentOrderDetail sampleDetail;

    @BeforeEach
    void setUp() {
        sampleResponse = new FulfillmentOrderResponse(
                "fo-1", "customer-1", null, null, null,
                0.0, null, null, null, false,
                FulfillmentOrderStatus.PROCESSING, null, null, null);

        SellerOrderResponse sellerOrderResponse = new SellerOrderResponse(
                "so-1", "fo-1", null, null, null, SellerOrderStatus.SHIPPED_TO_WAREHOUSE, null, null);

        sampleDetail = new AdminFulfillmentOrderDetail(sampleResponse, List.of(sellerOrderResponse));
    }

    // ═══════════════════ GET /api/admin/orders ════════════════════════════════

    @Test
    void getAllFulfillmentOrders_returns200WithList_whenAdminIsAuthenticated() throws Exception {
        when(adminOrderService.getAllFulfillmentOrders()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get(BASE_URL)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("fo-1"))
                .andExpect(jsonPath("$[0].status").value("PROCESSING"));

        verify(adminOrderService).getAllFulfillmentOrders();
    }

    @Test
    void getAllFulfillmentOrders_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminOrderService);
    }

    @Test
    void getAllFulfillmentOrders_returns403_whenSellerTriesToAccess() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminOrderService);
    }

    @Test
    void getAllFulfillmentOrders_returns403_whenCustomerTriesToAccess() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminOrderService);
    }

    @Test
    void getAllFulfillmentOrders_returnsEmptyList_whenNoOrdersExist() throws Exception {
        when(adminOrderService.getAllFulfillmentOrders()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ═══════════════════ GET /api/admin/orders/{id} ═══════════════════════════

    @Test
    void getFulfillmentOrderDetail_returns200WithDetail_whenOrderExists() throws Exception {
        when(adminOrderService.getFulfillmentOrderDetail("fo-1")).thenReturn(sampleDetail);

        mockMvc.perform(get(BASE_URL + "/fo-1")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fulfillmentOrder.id").value("fo-1"))
                .andExpect(jsonPath("$.fulfillmentOrder.status").value("PROCESSING"))
                .andExpect(jsonPath("$.sellerOrders[0].id").value("so-1"))
                .andExpect(jsonPath("$.sellerOrders[0].status").value("SHIPPED_TO_WAREHOUSE"));

        verify(adminOrderService).getFulfillmentOrderDetail("fo-1");
    }

    @Test
    void getFulfillmentOrderDetail_returns404_whenOrderDoesNotExist() throws Exception {
        when(adminOrderService.getFulfillmentOrderDetail("unknown"))
                .thenThrow(new OrderNotFoundException("FulfillmentOrder mit ID unknown nicht gefunden"));

        mockMvc.perform(get(BASE_URL + "/unknown")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("FulfillmentOrder mit ID unknown nicht gefunden"));
    }

    @Test
    void getFulfillmentOrderDetail_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/fo-1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminOrderService);
    }

    // ═══════════════════ PATCH /api/admin/orders/{id}/status ══════════════════

    @Test
    void updateFulfillmentOrderStatus_returns200_whenTransitionToReadyForFinalShipmentIsValid() throws Exception {
        FulfillmentOrderResponse updated = new FulfillmentOrderResponse(
                "fo-1", "customer-1", null, null, null,
                0.0, null, null, null, false,
                FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT, null, null, null);

        when(adminOrderService.updateFulfillmentOrderStatus("fo-1", FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT))
                .thenReturn(updated);

        mockMvc.perform(patch(BASE_URL + "/fo-1/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"READY_FOR_FINAL_SHIPMENT"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY_FOR_FINAL_SHIPMENT"));

        verify(adminOrderService).updateFulfillmentOrderStatus("fo-1", FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT);
    }

    @Test
    void updateFulfillmentOrderStatus_returns200_whenTransitionToCompletedIsValid() throws Exception {
        FulfillmentOrderResponse updated = new FulfillmentOrderResponse(
                "fo-1", "customer-1", null, null, null,
                0.0, null, null, null, false,
                FulfillmentOrderStatus.COMPLETED, null, null, null);

        when(adminOrderService.updateFulfillmentOrderStatus("fo-1", FulfillmentOrderStatus.COMPLETED))
                .thenReturn(updated);

        mockMvc.perform(patch(BASE_URL + "/fo-1/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateFulfillmentOrderStatus_returns400_whenTransitionIsInvalid() throws Exception {
        when(adminOrderService.updateFulfillmentOrderStatus("fo-1", FulfillmentOrderStatus.COMPLETED))
                .thenThrow(new IllegalArgumentException("Ungültiger Statusübergang von PROCESSING nach COMPLETED"));

        mockMvc.perform(patch(BASE_URL + "/fo-1/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ungültiger Statusübergang von PROCESSING nach COMPLETED"));
    }

    @Test
    void updateFulfillmentOrderStatus_returns422_whenNotAllSellerOrdersReady() throws Exception {
        when(adminOrderService.updateFulfillmentOrderStatus("fo-1", FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT))
                .thenThrow(new IllegalStateException("Nicht alle SellerOrders haben den Status SHIPPED_TO_WAREHOUSE oder höher"));

        mockMvc.perform(patch(BASE_URL + "/fo-1/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"READY_FOR_FINAL_SHIPMENT"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Nicht alle SellerOrders haben den Status SHIPPED_TO_WAREHOUSE oder höher"));
    }

    @Test
    void updateFulfillmentOrderStatus_returns404_whenOrderDoesNotExist() throws Exception {
        when(adminOrderService.updateFulfillmentOrderStatus("unknown", FulfillmentOrderStatus.READY_FOR_FINAL_SHIPMENT))
                .thenThrow(new OrderNotFoundException("FulfillmentOrder mit ID unknown nicht gefunden"));

        mockMvc.perform(patch(BASE_URL + "/unknown/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"READY_FOR_FINAL_SHIPMENT"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateFulfillmentOrderStatus_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/fo-1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"READY_FOR_FINAL_SHIPMENT"}
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminOrderService);
    }

    @Test
    void updateFulfillmentOrderStatus_returns403_whenSellerTriesToUpdate() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/fo-1/status")
                        .with(csrf())
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_SELLER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"READY_FOR_FINAL_SHIPMENT"}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminOrderService);
    }

    @Test
    void updateFulfillmentOrderStatus_returns403_whenCsrfTokenIsMissing() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/fo-1/status")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"READY_FOR_FINAL_SHIPMENT"}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminOrderService);
    }
}

