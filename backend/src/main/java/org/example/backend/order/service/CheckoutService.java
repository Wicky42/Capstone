package org.example.backend.order.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.common.model.Address;
import org.example.backend.common.service.OrderNumberService;
import org.example.backend.config.WarehouseProperties;
import org.example.backend.order.dto.CheckoutRequest;
import org.example.backend.order.dto.CheckoutResponse;
import org.example.backend.order.model.*;
import org.example.backend.order.repository.CustomerInvoiceRepository;
import org.example.backend.order.repository.FulfillmentOrderRepository;
import org.example.backend.order.repository.SellerOrderRepository;
import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.repository.ProductRepository;
import org.example.backend.user.model.Customer;
import org.example.backend.user.repository.UserRepository;
import org.example.backend.user.service.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Kapselt die gesamte Checkout-Logik:
 * Validierung → FulfillmentOrder → SellerOrders → CustomerInvoice → Customer-Profil aktualisieren
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final FulfillmentOrderRepository fulfillmentOrderRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final WarehouseProperties warehouseProperties;
    private final OrderNumberService orderNumberService;

    /**
     * Führt den vollständigen Checkout-Prozess durch.
     *
     * @param request  Checkout-Request mit Items sowie Liefer- und Rechnungsadresse
     * @return CheckoutResponse mit Bestell-ID, Rechnungs-ID, Status und Gesamtpreis
     */
    public CheckoutResponse checkout(CheckoutRequest request) {

        Customer customer = userService.getCurrentCustomer();

        List<OrderItem> items = validateAndBuildItems(request.items());

        // ── Schritt 3: Gesamtpreis serverseitig berechnen ────────────────────
        BigDecimal totalPrice = calculateTotalPrice(items);

        // ── Schritt 4: FulfillmentOrder erstellen und speichern ───────────────
        FulfillmentOrder fulfillmentOrder = buildFulfillmentOrder(
                customer.getId(), items, totalPrice,
                request.shippingAddress(), request.billingAddress(),
                orderNumberService.generateOrderNumber()
        );
        fulfillmentOrder = fulfillmentOrderRepository.save(fulfillmentOrder);

        // ── Schritt 5: SellerOrders pro Seller/Shop erstellen ─────────────────
        List<SellerOrder> sellerOrders = buildAndSaveSellerOrders(items, fulfillmentOrder.getId());

        // ── Schritt 6: sellerOrderIds in FulfillmentOrder eintragen ──────────
        List<String> sellerOrderIds = sellerOrders.stream()
                .map(SellerOrder::getId)
                .toList();
        fulfillmentOrder.setSellerOrderIds(sellerOrderIds);
        fulfillmentOrder = fulfillmentOrderRepository.save(fulfillmentOrder);

        // ── Schritt 7: CustomerInvoice erstellen und invoiceId eintragen ──────
        CustomerInvoice invoice = buildAndSaveInvoice(
                fulfillmentOrder.getId(), customer.getId(), items,
                totalPrice, request.billingAddress()
        );
        fulfillmentOrder.setInvoiceId(invoice.getId());
        fulfillmentOrderRepository.save(fulfillmentOrder);

        // ── Schritt 8: Customer-Profil um neue orderId erweitern ─────────────
        addOrderToCustomerProfile(customer, fulfillmentOrder.getId());

        // ── Schritt 9: Response zurückgeben ──────────────────────────────────
        return new CheckoutResponse(
                fulfillmentOrder.getOrderNumber(),
                invoice.getInvoiceNumber(),
                fulfillmentOrder.getStatus(),
                totalPrice
        );
    }

    // ═══════════════════ Private Hilfsmethoden ═══════════════════════════════

    /**
     * Validiert alle Items des Requests und erstellt daraus geprüfte OrderItem-Snapshots.
     * Serverseitige Re-Validierung: Jedes Produkt wird erneut aus der DB geprüft.
     *
     * @param requestItems Items aus dem Frontend-Request
     * @return Liste validierter OrderItem-Snapshots
     * @throws IllegalArgumentException  wenn die Itemliste leer ist
     * @throws ProductNotFoundException  wenn ein Produkt nicht existiert oder nicht ACTIVE ist
     */
    private List<OrderItem> validateAndBuildItems(OrderItem[] requestItems) {
        if (requestItems == null || requestItems.length == 0) {
            throw new IllegalArgumentException("Der Warenkorb darf nicht leer sein.");
        }

        List<OrderItem> validatedItems = new ArrayList<>();

        for (OrderItem requestItem : requestItems) {
            validateItemFields(requestItem);

            // Produkt aus der DB erneut laden und Status prüfen
            Product product = productRepository.findById(requestItem.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException());

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new IllegalStateException(
                        "Produkt '" + product.getName() + "' ist nicht mehr verfügbar."
                );
            }

            // Snapshot-Item aus DB-Daten + Client-Menge aufbauen
            OrderItem snapshot = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(product.getPrice())   // Preis immer aus der DB
                    .quantity(requestItem.getQuantity())
                    .titleImage(requestItem.getTitleImage())
                    .shopId(product.getShopId())
                    .sellerId(product.getSellerId())
                    .category(product.getCategory() != null ? product.getCategory().name() : null)
                    .snapshotCreatedAt(Instant.now())
                    .build();

            validatedItems.add(snapshot);
        }

        return validatedItems;
    }

    /**
     * Prüft Pflichtfelder eines einzelnen Request-Items.
     */
    private void validateItemFields(OrderItem item) {
        if (item.getProductId() == null || item.getProductId().isBlank()) {
            throw new IllegalArgumentException("Jedes Item benötigt eine Produkt-ID.");
        }
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Die Menge muss größer als 0 sein.");
        }
    }

    /**
     * Berechnet den Gesamtpreis aus den validierten (DB-geprüften) Items.
     * Nutzt BigDecimal für präzise Geldberechnung, gerundet auf 2 Nachkommastellen (HALF_UP).
     */
    private BigDecimal calculateTotalPrice(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Erstellt die FulfillmentOrder mit allen Metadaten.
     * Status ist initial CREATED, isPaid = true (MVP: Vorkasse simuliert).
     */
    private FulfillmentOrder buildFulfillmentOrder(
            String customerId,
            List<OrderItem> items,
            BigDecimal totalPrice,
            Address shippingAddress,
            Address billingAddress,
            String orderNumber
    ) {
        List<String> shopIds = items.stream()
                .map(OrderItem::getShopId)
                .distinct()
                .toList();

        return FulfillmentOrder.builder()
                .customerId(customerId)
                .orderNumber(orderNumber)
                .items(items)
                .shopIds(shopIds)
                .totalPrice(totalPrice)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .isPaid(true)
                .status(FulfillmentOrderStatus.CREATED)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * Gruppiert die Items nach Seller und erstellt pro Seller eine SellerOrder.
     * Alle SellerOrders erhalten die Lageradresse aus der Konfiguration.
     *
     * @param items            validierte OrderItems
     * @param fulfillmentOrderId ID der übergeordneten FulfillmentOrder
     * @return gespeicherte SellerOrders
     */
    private List<SellerOrder> buildAndSaveSellerOrders(List<OrderItem> items, String fulfillmentOrderId) {
        // Items nach sellerId gruppieren – ein Seller bekommt genau eine SellerOrder
        Map<String, List<OrderItem>> itemsBySeller = items.stream()
                .collect(Collectors.groupingBy(OrderItem::getSellerId));

        Address warehouseAddress = buildWarehouseAddress();
        List<SellerOrder> savedOrders = new ArrayList<>();

        for (Map.Entry<String, List<OrderItem>> entry : itemsBySeller.entrySet()) {
            String sellerId = entry.getKey();
            List<OrderItem> sellerItems = entry.getValue();

            // shopId aus dem ersten Item (alle Items eines Sellers gehören zu einem Shop)
            String shopId = sellerItems.getFirst().getShopId();

            SellerOrder sellerOrder = SellerOrder.builder()
                    .fulfillmentOrderId(fulfillmentOrderId)
                    .orderNumber(orderNumberService.generateSellerOrderNumber())
                    .sellerId(sellerId)
                    .shopId(shopId)
                    .items(sellerItems)
                    .warehouseAddress(warehouseAddress)
                    .status(SellerOrderStatus.CREATED)
                    .createdAt(Instant.now())
                    .build();

            savedOrders.add(sellerOrderRepository.save(sellerOrder));
        }

        return savedOrders;
    }

    private CustomerInvoice buildAndSaveInvoice(
            String fulfillmentOrderId,
            String customerId,
            List<OrderItem> items,
            BigDecimal totalAmount,
            Address billingAddress
    ) {
        CustomerInvoice invoice = CustomerInvoice.builder()
                .fulfillmentOrderId(fulfillmentOrderId)
                .customerId(customerId)
                .items(items)
                .totalAmount(totalAmount)
                .billingAddress(billingAddress)
                .invoiceNumber(orderNumberService.generateInvoiceNumber())
                .createdAt(Instant.now())
                .build();

        return customerInvoiceRepository.save(invoice);
    }
    private void addOrderToCustomerProfile(Customer customer, String fulfillmentOrderId) {
        List<String> orderIds = new ArrayList<>(
                customer.getOrderIds() != null ? customer.getOrderIds() : Collections.emptyList()
        );
        orderIds.add(fulfillmentOrderId);
        customer.setOrderIds(orderIds);
        userRepository.save(customer);
    }

    /**
     * Baut das Address-Objekt für das Zentrallager aus den konfigurierten Properties.
     */
    private Address buildWarehouseAddress() {
        return new Address(
                warehouseProperties.getStreet(),
                warehouseProperties.getHouseNumber(),
                warehouseProperties.getPostalCode(),
                warehouseProperties.getCity(),
                warehouseProperties.getCountry()
        );
    }
}
