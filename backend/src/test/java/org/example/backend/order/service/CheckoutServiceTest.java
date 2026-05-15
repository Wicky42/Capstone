package org.example.backend.order.service;

import org.example.backend.common.exception.ProductNotFoundException;
import org.example.backend.common.model.Address;
import org.example.backend.config.WarehouseProperties;
import org.example.backend.order.dto.CheckoutRequest;
import org.example.backend.order.dto.CheckoutResponse;
import org.example.backend.order.model.*;
import org.example.backend.order.repository.CustomerInvoiceRepository;
import org.example.backend.order.repository.FulfillmentOrderRepository;
import org.example.backend.order.repository.SellerOrderRepository;
import org.example.backend.product.model.Product;
import org.example.backend.product.model.ProductCategory;
import org.example.backend.product.model.ProductStatus;
import org.example.backend.product.repository.ProductRepository;
import org.example.backend.user.model.Customer;
import org.example.backend.user.model.User;
import org.example.backend.user.repository.UserRepository;
import org.example.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    // ─── Mocks ───────────────────────────────────────────────────────────────

    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private FulfillmentOrderRepository fulfillmentOrderRepository;
    @Mock private SellerOrderRepository sellerOrderRepository;
    @Mock private CustomerInvoiceRepository customerInvoiceRepository;
    @Mock private WarehouseProperties warehouseProperties;

    @InjectMocks
    private CheckoutService checkoutService;

    // ─── Konstanten ──────────────────────────────────────────────────────────

    private static final String CUSTOMER_ID    = "customer-1";
    private static final String SELLER_ID_A    = "seller-1";
    private static final String SELLER_ID_B    = "seller-2";
    private static final String SHOP_ID_A      = "shop-1";
    private static final String SHOP_ID_B      = "shop-2";
    private static final String PRODUCT_ID_A   = "product-honey";
    private static final String PRODUCT_ID_B   = "product-jam";
    private static final String FULFILLMENT_ID = "fo-1";
    private static final String INVOICE_ID     = "inv-1";

    // ─── Testdaten ────────────────────────────────────────────────────────────

    private Customer customer;
    private Product productHoney;
    private Product productJam;
    private Address shippingAddress;
    private Address billingAddress;

    /** Zähler für SellerOrder-IDs (mehrere Saves in einem Test) */
    private final AtomicInteger sellerOrderCounter = new AtomicInteger(1);

    // ─── Setup ───────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        sellerOrderCounter.set(1);

        // Customer mit leerer orderIds-Liste
        customer = Customer.builder()
                .id(CUSTOMER_ID)
                .role(User.Role.CUSTOMER)
                .name("Max Muster")
                .email("max@example.com")
                .orderIds(new ArrayList<>())
                .build();

        // Aktives Produkt von Seller A
        productHoney = Product.builder()
                .id(PRODUCT_ID_A)
                .sellerId(SELLER_ID_A)
                .shopId(SHOP_ID_A)
                .name("Waldhonig 500g")
                .price(new BigDecimal("8.99"))
                .category(ProductCategory.HONIG)
                .status(ProductStatus.ACTIVE)
                .build();

        // Aktives Produkt von Seller B
        productJam = Product.builder()
                .id(PRODUCT_ID_B)
                .sellerId(SELLER_ID_B)
                .shopId(SHOP_ID_B)
                .name("Erdbeermarmelade 250g")
                .price(new BigDecimal("5.49"))
                .category(null)
                .status(ProductStatus.ACTIVE)
                .build();

        shippingAddress = new Address("Musterstrasse", "12a", "12345", "Berlin", "Deutschland");
        billingAddress  = new Address("Musterstrasse", "12a", "12345", "Berlin", "Deutschland");

        // ── Standard-Mocks für alle Tests ──────────────────────────────────
        // lenient(): Diese Stubs werden nicht in jedem Test benötigt (z. B. Fehlerfall-Tests
        //            brechen früh ab, bevor Repositories aufgerufen werden).

        lenient().when(userService.getCurrentCustomer()).thenReturn(customer);

        // Lageradresse aus Konfiguration
        lenient().when(warehouseProperties.getStreet()).thenReturn("Lagerstrasse");
        lenient().when(warehouseProperties.getHouseNumber()).thenReturn("1");
        lenient().when(warehouseProperties.getPostalCode()).thenReturn("10115");
        lenient().when(warehouseProperties.getCity()).thenReturn("Berlin");
        lenient().when(warehouseProperties.getCountry()).thenReturn("Deutschland");

        // Repositories: save() gibt das übergebene Objekt mit generierter ID zurück
        lenient().when(fulfillmentOrderRepository.save(any(FulfillmentOrder.class))).thenAnswer(inv -> {
            FulfillmentOrder fo = inv.getArgument(0);
            fo.setId(FULFILLMENT_ID);
            return fo;
        });

        lenient().when(sellerOrderRepository.save(any(SellerOrder.class))).thenAnswer(inv -> {
            SellerOrder so = inv.getArgument(0);
            so.setId("so-" + sellerOrderCounter.getAndIncrement());
            return so;
        });

        lenient().when(customerInvoiceRepository.save(any(CustomerInvoice.class))).thenAnswer(inv -> {
            CustomerInvoice ci = inv.getArgument(0);
            ci.setId(INVOICE_ID);
            return ci;
        });

        lenient().when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ─── Hilfsmethoden ────────────────────────────────────────────────────────

    /**
     * Erstellt ein minimales Request-Item (unitPrice des Clients wird vom Server ignoriert).
     */
    private OrderItem buildRequestItem(String productId, int quantity) {
        return OrderItem.builder()
                .productId(productId)
                .quantity(quantity)
                .titleImage("/images/" + productId + ".jpg")
                .build();
    }

    private CheckoutRequest buildSingleItemRequest() {
        return new CheckoutRequest(
                new OrderItem[]{buildRequestItem(PRODUCT_ID_A, 2)},
                shippingAddress,
                billingAddress
        );
    }

    private CheckoutRequest buildMultiSellerRequest() {
        return new CheckoutRequest(
                new OrderItem[]{
                        buildRequestItem(PRODUCT_ID_A, 2),
                        buildRequestItem(PRODUCT_ID_B, 1)
                },
                shippingAddress,
                billingAddress
        );
    }

    // ═══════════════════ Happy-Path Tests ════════════════════════════════════

    @Test
    void checkout_returnsValidResponse_whenSingleActiveProductInCart() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));

        // when
        CheckoutResponse response = checkoutService.checkout(buildSingleItemRequest());

        // then
        assertThat(response.orderId()).isEqualTo(FULFILLMENT_ID);
        assertThat(response.invoiceId()).isEqualTo(INVOICE_ID);
        assertThat(response.status()).isEqualTo(FulfillmentOrderStatus.CREATED);
        assertThat(response.totalPrice()).isEqualTo(17.98); // 8.99 * 2
    }

    @Test
    void checkout_calculatesTotalPriceFromDatabase_notFromRequest() {
        // given – Request-Item hat absichtlich falschen unitPrice (wird ignoriert)
        OrderItem requestItemWithWrongPrice = OrderItem.builder()
                .productId(PRODUCT_ID_A)
                .quantity(1)
                .unitPrice(999.99) // Client-Preis – darf NICHT übernommen werden
                .build();
        CheckoutRequest request = new CheckoutRequest(
                new OrderItem[]{requestItemWithWrongPrice}, shippingAddress, billingAddress
        );
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));

        // when
        CheckoutResponse response = checkoutService.checkout(request);

        // then – Preis muss aus der DB kommen: 8.99, nicht 999.99
        assertThat(response.totalPrice()).isEqualTo(8.99);
    }

    @Test
    void checkout_savesFulfillmentOrderWithStatusCreatedAndIsPaidTrue() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        ArgumentCaptor<FulfillmentOrder> captor = ArgumentCaptor.forClass(FulfillmentOrder.class);

        // when
        checkoutService.checkout(buildSingleItemRequest());

        // then
        verify(fulfillmentOrderRepository, atLeast(1)).save(captor.capture());
        FulfillmentOrder savedOrder = captor.getAllValues().getFirst();
        assertThat(savedOrder.getStatus()).isEqualTo(FulfillmentOrderStatus.CREATED);
        assertThat(savedOrder.isPaid()).isTrue();
        assertThat(savedOrder.getCustomerId()).isEqualTo(CUSTOMER_ID);
    }

    @Test
    void checkout_savesShippingAndBillingAddressAsSnapshot() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        ArgumentCaptor<FulfillmentOrder> captor = ArgumentCaptor.forClass(FulfillmentOrder.class);

        // when
        checkoutService.checkout(buildSingleItemRequest());

        // then
        verify(fulfillmentOrderRepository, atLeast(1)).save(captor.capture());
        FulfillmentOrder savedOrder = captor.getAllValues().getFirst();
        assertThat(savedOrder.getShippingAddress().getCity()).isEqualTo("Berlin");
        assertThat(savedOrder.getBillingAddress().getStreet()).isEqualTo("Musterstrasse");
    }

    @Test
    void checkout_createsExactlyOneSellerOrder_whenAllItemsFromOneSeller() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));

        // when
        checkoutService.checkout(buildSingleItemRequest());

        // then
        verify(sellerOrderRepository, times(1)).save(any(SellerOrder.class));
    }

    @Test
    void checkout_createsTwoSellerOrders_whenItemsFromTwoDifferentSellers() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        when(productRepository.findById(PRODUCT_ID_B)).thenReturn(Optional.of(productJam));

        // when
        checkoutService.checkout(buildMultiSellerRequest());

        // then – ein Seller = eine SellerOrder
        verify(sellerOrderRepository, times(2)).save(any(SellerOrder.class));
    }

    @Test
    void checkout_setsCorrectSellerIdOnEachSellerOrder() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        when(productRepository.findById(PRODUCT_ID_B)).thenReturn(Optional.of(productJam));
        ArgumentCaptor<SellerOrder> captor = ArgumentCaptor.forClass(SellerOrder.class);

        // when
        checkoutService.checkout(buildMultiSellerRequest());

        // then
        verify(sellerOrderRepository, times(2)).save(captor.capture());
        List<String> savedSellerIds = captor.getAllValues().stream()
                .map(SellerOrder::getSellerId)
                .toList();
        assertThat(savedSellerIds).containsExactlyInAnyOrder(SELLER_ID_A, SELLER_ID_B);
    }

    @Test
    void checkout_setsWarehouseAddressFromConfigOnSellerOrders() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        ArgumentCaptor<SellerOrder> captor = ArgumentCaptor.forClass(SellerOrder.class);

        // when
        checkoutService.checkout(buildSingleItemRequest());

        // then
        verify(sellerOrderRepository).save(captor.capture());
        Address warehouse = captor.getValue().getWarehouseAddress();
        assertThat(warehouse.getStreet()).isEqualTo("Lagerstrasse");
        assertThat(warehouse.getCity()).isEqualTo("Berlin");
        assertThat(warehouse.getPostalCode()).isEqualTo("10115");
    }

    @Test
    void checkout_createsInvoiceWithCorrectBigDecimalTotalAmount() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        ArgumentCaptor<CustomerInvoice> captor = ArgumentCaptor.forClass(CustomerInvoice.class);

        // when
        checkoutService.checkout(buildSingleItemRequest());

        // then – 8.99 * 2 = 17.98 als BigDecimal gespeichert
        verify(customerInvoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalAmount())
                .isEqualByComparingTo(new BigDecimal("17.98"));
    }

    @Test
    void checkout_createsInvoiceWithNonBlankInvoiceNumber() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        ArgumentCaptor<CustomerInvoice> captor = ArgumentCaptor.forClass(CustomerInvoice.class);

        // when
        checkoutService.checkout(buildSingleItemRequest());

        // then
        verify(customerInvoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getInvoiceNumber())
                .isNotBlank()
                .startsWith("INV-");
    }

    @Test
    void checkout_setsInvoiceIdOnFulfillmentOrder() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        ArgumentCaptor<FulfillmentOrder> captor = ArgumentCaptor.forClass(FulfillmentOrder.class);

        // when
        checkoutService.checkout(buildSingleItemRequest());

        // then – letzter Save muss die invoiceId enthalten
        verify(fulfillmentOrderRepository, atLeast(1)).save(captor.capture());
        FulfillmentOrder lastSaved = captor.getAllValues().getLast();
        assertThat(lastSaved.getInvoiceId()).isEqualTo(INVOICE_ID);
    }

    @Test
    void checkout_addsOrderIdToCustomerProfile() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);

        // when
        checkoutService.checkout(buildSingleItemRequest());

        // then
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderIds()).contains(FULFILLMENT_ID);
    }

    @Test
    void checkout_preservesExistingOrderIdsWhenAddingNew() {
        // given – Customer hat bereits eine Bestellung
        customer.setOrderIds(new ArrayList<>(List.of("fo-old-1")));
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);

        // when
        checkoutService.checkout(buildSingleItemRequest());

        // then – alte UND neue orderId müssen enthalten sein
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderIds())
                .containsExactlyInAnyOrder("fo-old-1", FULFILLMENT_ID);
    }

    @Test
    void checkout_buildsOrderItemSnapshotFromDatabase_notFromRequest() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        ArgumentCaptor<FulfillmentOrder> captor = ArgumentCaptor.forClass(FulfillmentOrder.class);

        // when
        checkoutService.checkout(buildSingleItemRequest());

        // then – Snapshot enthält DB-Daten (Name, shopId, sellerId aus DB)
        verify(fulfillmentOrderRepository, atLeast(1)).save(captor.capture());
        OrderItem snapshot = captor.getAllValues().getFirst().getItems().getFirst();
        assertThat(snapshot.getProductName()).isEqualTo("Waldhonig 500g");
        assertThat(snapshot.getShopId()).isEqualTo(SHOP_ID_A);
        assertThat(snapshot.getSellerId()).isEqualTo(SELLER_ID_A);
        assertThat(snapshot.getUnitPrice()).isEqualTo(8.99);
        assertThat(snapshot.getSnapshotCreatedAt()).isNotNull();
    }

    // ═══════════════════ Validierungs-Tests ══════════════════════════════════

    @Test
    void checkout_throwsIllegalArgumentException_whenItemsArrayIsNull() {
        // given
        CheckoutRequest request = new CheckoutRequest(null, shippingAddress, billingAddress);

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("leer");

        verifyNoInteractions(productRepository, fulfillmentOrderRepository);
    }

    @Test
    void checkout_throwsIllegalArgumentException_whenItemsArrayIsEmpty() {
        // given
        CheckoutRequest request = new CheckoutRequest(new OrderItem[]{}, shippingAddress, billingAddress);

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("leer");

        verifyNoInteractions(productRepository, fulfillmentOrderRepository);
    }

    @Test
    void checkout_throwsIllegalArgumentException_whenProductIdIsBlank() {
        // given
        OrderItem invalidItem = OrderItem.builder().productId("  ").quantity(1).build();
        CheckoutRequest request = new CheckoutRequest(
                new OrderItem[]{invalidItem}, shippingAddress, billingAddress
        );

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Produkt-ID");

        verifyNoInteractions(fulfillmentOrderRepository, sellerOrderRepository);
    }

    @Test
    void checkout_throwsIllegalArgumentException_whenProductIdIsNull() {
        // given
        OrderItem invalidItem = OrderItem.builder().productId(null).quantity(1).build();
        CheckoutRequest request = new CheckoutRequest(
                new OrderItem[]{invalidItem}, shippingAddress, billingAddress
        );

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Produkt-ID");
    }

    @Test
    void checkout_throwsIllegalArgumentException_whenQuantityIsZero() {
        // given
        OrderItem invalidItem = OrderItem.builder().productId(PRODUCT_ID_A).quantity(0).build();
        CheckoutRequest request = new CheckoutRequest(
                new OrderItem[]{invalidItem}, shippingAddress, billingAddress
        );

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Menge");

        verifyNoInteractions(fulfillmentOrderRepository, sellerOrderRepository);
    }

    @Test
    void checkout_throwsIllegalArgumentException_whenQuantityIsNegative() {
        // given
        OrderItem invalidItem = OrderItem.builder().productId(PRODUCT_ID_A).quantity(-3).build();
        CheckoutRequest request = new CheckoutRequest(
                new OrderItem[]{invalidItem}, shippingAddress, billingAddress
        );

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Menge");
    }

    @Test
    void checkout_throwsProductNotFoundException_whenProductDoesNotExistInDatabase() {
        // given
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(buildSingleItemRequest()))
                .isInstanceOf(ProductNotFoundException.class);

        verifyNoInteractions(fulfillmentOrderRepository, sellerOrderRepository, customerInvoiceRepository);
    }

    @Test
    void checkout_throwsIllegalStateException_whenProductIsInactive() {
        // given
        Product inactiveProduct = Product.builder()
                .id(PRODUCT_ID_A).sellerId(SELLER_ID_A).shopId(SHOP_ID_A)
                .name("Abgelaufener Honig").price(new BigDecimal("8.99"))
                .status(ProductStatus.INACTIVE)
                .build();
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(inactiveProduct));

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(buildSingleItemRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht mehr verfügbar");

        verifyNoInteractions(fulfillmentOrderRepository, sellerOrderRepository);
    }

    @Test
    void checkout_throwsIllegalStateException_whenProductIsDraft() {
        // given
        Product draftProduct = Product.builder()
                .id(PRODUCT_ID_A).sellerId(SELLER_ID_A).shopId(SHOP_ID_A)
                .name("Entwurf Honig").price(new BigDecimal("8.99"))
                .status(ProductStatus.DRAFT)
                .build();
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(draftProduct));

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(buildSingleItemRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht mehr verfügbar");
    }

    @Test
    void checkout_throwsIllegalStateException_whenProductIsRecalled() {
        // given
        Product recalledProduct = Product.builder()
                .id(PRODUCT_ID_A).sellerId(SELLER_ID_A).shopId(SHOP_ID_A)
                .name("Zurückgerufener Honig").price(new BigDecimal("8.99"))
                .status(ProductStatus.RECALLED)
                .build();
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(recalledProduct));

        // when / then
        assertThatThrownBy(() -> checkoutService.checkout(buildSingleItemRequest()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ═══════════════════ Preisberechnung ════════════════════════════════════

    @Test
    void checkout_calculatesTotalPriceCorrectlyForMultipleItems() {
        // given – 2 * 8.99 + 1 * 5.49 = 23.47
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(productHoney));
        when(productRepository.findById(PRODUCT_ID_B)).thenReturn(Optional.of(productJam));

        // when
        CheckoutResponse response = checkoutService.checkout(buildMultiSellerRequest());

        // then
        assertThat(response.totalPrice()).isEqualTo(23.47);
    }

    @Test
    void checkout_roundsTotalPriceToTwoDecimalPlaces() {
        // given – Preis der ggf. Rundung erfordert (z. B. 3 * 0.10 = 0.30)
        Product cheapProduct = Product.builder()
                .id(PRODUCT_ID_A).sellerId(SELLER_ID_A).shopId(SHOP_ID_A)
                .name("Mini-Artikel").price(new BigDecimal("0.10"))
                .status(ProductStatus.ACTIVE).build();
        OrderItem item = buildRequestItem(PRODUCT_ID_A, 3);
        CheckoutRequest request = new CheckoutRequest(
                new OrderItem[]{item}, shippingAddress, billingAddress
        );
        when(productRepository.findById(PRODUCT_ID_A)).thenReturn(Optional.of(cheapProduct));

        // when
        CheckoutResponse response = checkoutService.checkout(request);

        // then
        assertThat(response.totalPrice()).isEqualTo(0.30);
    }
}

