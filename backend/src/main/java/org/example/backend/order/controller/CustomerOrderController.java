package org.example.backend.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.order.dto.CustomerInvoiceResponse;
import org.example.backend.order.dto.FulfillmentOrderResponse;
import org.example.backend.order.repository.CustomerInvoiceRepository;
import org.example.backend.order.repository.FulfillmentOrderRepository;
import org.example.backend.user.model.Customer;
import org.example.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final UserService userService;
    private final FulfillmentOrderRepository fulfillmentOrderRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;

    @GetMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<FulfillmentOrderResponse>> getCustomerOrders() {
        Customer currentCustomer = userService.getCurrentCustomer();
        return ResponseEntity.ok(
                fulfillmentOrderRepository.findByCustomerId(currentCustomer.getId())
                .stream()
                .map(FulfillmentOrderResponse::from)
                .toList()
        );
    }

    @GetMapping("/orders/{orderNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<FulfillmentOrderResponse> getCustomerOrderById(@PathVariable String orderNumber) {
        Customer currentCustomer = userService.getCurrentCustomer();
        return fulfillmentOrderRepository.findByOrderNumber(orderNumber)
                .filter(order -> order.getCustomerId().equals(currentCustomer.getId()))
                .map(FulfillmentOrderResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerInvoiceResponse> getCustomerInvoiceById(@PathVariable String id) {
        Customer currentCustomer = userService.getCurrentCustomer();
        return customerInvoiceRepository.findById(id)
                .map(CustomerInvoiceResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
