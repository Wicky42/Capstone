# Phase 6 – Tickets

> Vollständige Spezifikation: [`docs/todo/phase-6-checkout-flows.md`](phase-6-checkout-flows.md)

---

## Backend-Tickets

---

### TICKET-6.1 – Models & Repositories

**Ziel:** Alle Datenbankmodelle und Repositories für die Bestelllogik anlegen.

**Dateien (neu):**
- `order/model/FulfillmentOrder.java`
- `order/model/SellerOrder.java`
- `order/model/OrderItem.java`
- `order/model/FulfillmentOrderStatus.java` (Enum)
- `order/model/SellerOrderStatus.java` (Enum)
- `order/model/CustomerInvoice.java`
- `order/repository/FulfillmentOrderRepository.java`
- `order/repository/SellerOrderRepository.java`
- `order/repository/CustomerInvoiceRepository.java`

**Felder `FulfillmentOrder`:**
- `id`, `customerId`, `items`, `sellerOrderIds`, `shopIds`
- `totalPrice`, `shippingAddress`, `billingAddress`
- `invoiceId`, `isPaid`, `status`, `createdAt`, `updatedAt`

**Felder `SellerOrder`:**
- `id`, `fulfillmentOrderId`, `sellerId`, `shopId`
- `items`, `warehouseAddress`, `status`, `createdAt`, `updatedAt`

**Felder `OrderItem` (embedded, Snapshot):**
- `productId`, `productName`, `unitPrice`, `quantity`
- `titleImage`, `shopId`, `sellerId`, `category`, `snapshotCreatedAt`

**Felder `CustomerInvoice`:**
- `id`, `fulfillmentOrderId`, `customerId`
- `items`, `totalAmount`, `billingAddress`
- `invoiceNumber`, `createdAt`

**Akzeptanzkriterien:**
- [ ] Alle Klassen annotiert mit `@Document`, `@Id`, `@Field`
- [ ] Repositories mit Spring Data MongoDB
- [ ] Enums `FulfillmentOrderStatus` und `SellerOrderStatus` vollständig
- [ ] `OrderItem` als eingebettete Klasse ohne eigene Collection

---

### TICKET-6.2 – CheckoutService

**Ziel:** Gesamte Checkout-Logik in einem Service kapseln.

**Datei (neu):** `order/service/CheckoutService.java`

**Methode:** `CheckoutResponse checkout(CheckoutRequest request, String customerId)`

**Ablauf:**
1. Validierung: Items nicht leer, alle Items `ACTIVE`, `quantity > 0`, `unitPrice > 0`
2. `totalPrice` serverseitig berechnen (nie aus Request übernehmen)
3. `FulfillmentOrder` mit Status `CREATED` und `isPaid = true` speichern
4. Items nach `sellerId` / `shopId` gruppieren
5. Für jede Gruppe eine `SellerOrder` mit `warehouseAddress` aus `WarehouseProperties` speichern
6. `sellerOrderIds` in `FulfillmentOrder` eintragen und aktualisieren
7. `CustomerInvoice` erstellen und `invoiceId` in `FulfillmentOrder` eintragen
8. `customer.orderIds` um neue `fulfillmentOrderId` erweitern
9. `CheckoutResponse` zurückgeben

**Akzeptanzkriterien:**
- [ ] `totalPrice` wird immer serverseitig berechnet
- [ ] Jeder Seller erhält genau eine `SellerOrder` pro Checkout
- [ ] `CustomerInvoice` wird immer erzeugt
- [ ] Produktstatus wird aus der DB geprüft (serverseitige Re-Validierung)
- [ ] `isPaid = true` direkt bei Erstellung (MVP)
- [ ] Bei Fehler: kein Teildatensatz in der DB

---

### TICKET-6.3 – CheckoutController

**Ziel:** REST-Endpunkt für den Checkout bereitstellen.

**Datei (neu):** `order/controller/CheckoutController.java`

**Endpunkt:**
```http
POST /api/orders/checkout
```

**Zugriff:** Nur für `CUSTOMER`

**Request:** `CheckoutRequest`

**Response:** `201 Created` + `CheckoutResponse`

**DTOs (neu):**
- `order/dto/CheckoutRequest.java`
  - `List<OrderItemRequest> items`
  - `AddressSnapshot shippingAddress`
  - `AddressSnapshot billingAddress`
- `order/dto/CheckoutResponse.java`
  - `String orderId`
  - `String invoiceId`
  - `String status`
  - `double totalPrice`
- `order/dto/OrderItemRequest.java`
  - `productId`, `productName`, `unitPrice`, `quantity`, `titleImage`, `shopId`, `sellerId`

**Akzeptanzkriterien:**
- [ ] Endpunkt mit `@PreAuthorize("hasRole('CUSTOMER')")` geschützt
- [ ] Validierung mit `@Valid` und `@NotNull`, `@NotEmpty`
- [ ] Gibt `201 Created` bei Erfolg zurück
- [ ] Gibt `400 Bad Request` bei ungültigen Items zurück
- [ ] Gibt `403 Forbidden` zurück wenn kein CUSTOMER

---

### TICKET-6.4 – CustomerOrderController

**Ziel:** Customer kann seine eigenen Bestellungen und Rechnungen einsehen.

**Datei (neu):** `order/controller/CustomerOrderController.java`

**Endpunkte:**

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/customer/orders` | Alle FulfillmentOrders des eingeloggten Customers |
| `GET` | `/api/customer/orders/{id}` | Detail einer FulfillmentOrder |
| `GET` | `/api/customer/invoices/{id}` | CustomerInvoice abrufen |

**DTOs (neu):**
- `FulfillmentOrderResponse` – alle Felder der FulfillmentOrder
- `CustomerInvoiceResponse` – alle Felder der CustomerInvoice

**Akzeptanzkriterien:**
- [ ] Customer sieht nur seine eigenen Bestellungen
- [ ] Gibt `404` zurück wenn Bestellung nicht existiert oder nicht dem Customer gehört
- [ ] Service-Klasse: `CustomerOrderService`

---

### TICKET-6.5 – SellerOrderController

**Ziel:** Seller kann seine Teilbestellungen einsehen und den Status ändern.

**Datei (neu):** `order/controller/SellerOrderController.java`

**Endpunkte:**

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/seller/orders` | Alle SellerOrders des eingeloggten Sellers |
| `GET` | `/api/seller/orders/{id}` | Detail einer SellerOrder |
| `PATCH` | `/api/seller/orders/{id}/status` | Status der SellerOrder ändern |

**Erlaubte Statusübergänge:**
- `CREATED` → `CONFIRMED`
- `CONFIRMED` → `IN_PREPARATION`
- `IN_PREPARATION` → `SHIPPED_TO_WAREHOUSE`

**Akzeptanzkriterien:**
- [ ] Seller sieht nur eigene SellerOrders
- [ ] Ungültige Statusübergänge werden abgelehnt (`400 Bad Request`)
- [ ] Service-Klasse: `SellerOrderService`

---

### TICKET-6.6 – AdminOrderController

**Ziel:** Admin kann alle Bestellungen einsehen und den Gesamtstatus steuern.

**Datei (neu):** `order/controller/AdminOrderController.java`

**Endpunkte:**

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/admin/orders` | Alle FulfillmentOrders |
| `GET` | `/api/admin/orders/{id}` | Detail inkl. aller SellerOrders |
| `PATCH` | `/api/admin/orders/{id}/status` | Gesamtstatus steuern |

**Erlaubte Statusübergänge:**
- `PROCESSING` → `READY_FOR_FINAL_SHIPMENT` (nur wenn alle SellerOrders ≥ `SHIPPED_TO_WAREHOUSE`)
- `READY_FOR_FINAL_SHIPMENT` → `COMPLETED`

**Akzeptanzkriterien:**
- [ ] Admin sieht alle FulfillmentOrders
- [ ] Status `READY_FOR_FINAL_SHIPMENT` nur wenn alle SellerOrders bereit
- [ ] Service-Klasse: `AdminOrderService`

---

### TICKET-6.7 – WarehouseProperties

**Ziel:** Zentrallager-Adresse konfigurierbar machen, nicht hardkodiert.

**Datei (neu):** `config/WarehouseProperties.java`

**Konfiguration in `application.properties`:**
```properties
warehouse.street=Lagerstraße
warehouse.house-number=1
warehouse.postal-code=10115
warehouse.city=Berlin
warehouse.country=Deutschland
```

**Akzeptanzkriterien:**
- [ ] `@ConfigurationProperties(prefix = "warehouse")`
- [ ] Wird in `CheckoutService` per Dependency Injection genutzt
- [ ] Adresse wird in jede neue `SellerOrder` als `warehouseAddress` eingetragen

---

## Frontend-Tickets

---

### TICKET-6.8 – Auth-Guard auf Checkout

**Ziel:** Checkout darf nur von eingeloggten Customers gestartet werden.

**Dateien (geändert):**
- `pages/cart/CartPage.tsx` – „Zur Kasse"-Button aktivieren
- `App.tsx` – Route `/checkout` mit Guard versehen

**Logik:**
- Ist der User nicht eingeloggt → `sessionStorage.setItem('redirectAfterLogin', '/checkout')` → Weiterleitung zu `/register` mit vorausgewählter Rolle `CUSTOMER`
- Ist der User `SELLER` → Hinweistext anzeigen, kein Checkout möglich
- Ist der User `CUSTOMER` → Weiterleitung zu `/checkout`

**Akzeptanzkriterien:**
- [ ] Nicht eingeloggte User landen auf der Registrierungsseite mit Hinweis
- [ ] Nach Login wird der Redirect aus `sessionStorage` aufgelöst
- [ ] SELLER sehen einen klaren Hinweis und können nicht weiter

---

### TICKET-6.9 – CheckoutPage Schritt 1: Adressformular

**Ziel:** Customer gibt Liefer- und Rechnungsadresse ein.

**Dateien (neu):**
- `pages/checkout/CheckoutPage.tsx` – Schritt 1 (Adresse)
- `components/checkout/AddressForm.tsx` – Wiederverwendbares Adressformular

**Felder:**
- Straße, Hausnummer, PLZ, Stadt, Land
- Checkbox: „Rechnungsadresse = Lieferadresse"

**Validierung:**
- Alle Felder Pflicht
- PLZ: 5 Ziffern

**Vorausfüllen:**
- Falls `customer.shippingAddress` vorhanden → Felder vorausfüllen

**Akzeptanzkriterien:**
- [ ] „Weiter"-Button nur aktiv bei valider Adresse
- [ ] Adresse wird in lokalem State für Schritt 2 gespeichert
- [ ] Checkbox für gleiche Rechnungsadresse funktioniert

---

### TICKET-6.10 – CheckoutPage Schritt 2: Bestellübersicht + Absenden

**Ziel:** Customer sieht eine Zusammenfassung und kann die Bestellung absenden.

**Dateien (neu/geändert):**
- `pages/checkout/CheckoutPage.tsx` – Schritt 2 (Übersicht)
- `components/checkout/OrderSummary.tsx` – Zusammenfassungskomponente
- `services/orderService.ts` – `checkout(request)` Funktion

**Angezeigt:**
- Items gruppiert nach Shop
- Gesamtpreis
- Liefer- und Rechnungsadresse
- Hinweis: „Vorkasse simuliert"

**Bei Klick auf „Jetzt kaufen":**
1. `POST /api/orders/checkout` aufrufen
2. Bei Erfolg: `CartContext.clearCart()` aufrufen
3. Weiterleitung zu `/orders/{orderId}/confirmation`
4. Bei Fehler: Fehlermeldung anzeigen, Seite bleibt offen

**Akzeptanzkriterien:**
- [ ] Preise werden aus dem lokalen Cart-State gerechnet (kein eigener API-Call für Preise)
- [ ] Button deaktiviert während des API-Calls (kein doppeltes Absenden)
- [ ] `localStorage` wird nach Erfolg geleert
- [ ] Fehlerfall wird dem Nutzer verständlich angezeigt

---

### TICKET-6.11 – OrderConfirmationPage

**Ziel:** Bestätigungsseite nach erfolgreichem Checkout.

**Datei (neu):** `pages/checkout/OrderConfirmationPage.tsx`

**Route:** `/orders/:orderId/confirmation`

**Angezeigt:**
- Erfolgsnachricht
- Bestellnummer
- Rechnungsnummer
- Gesamtpreis
- Liefer- und Rechnungsadresse
- Bestellte Produkte
- Link: „Zu meinen Bestellungen" → `/account/orders`
- Link: „Weiter einkaufen" → `/`

**Akzeptanzkriterien:**
- [ ] Daten werden über `GET /api/customer/orders/{id}` geladen
- [ ] Seite ist nur mit eingeloggtem CUSTOMER erreichbar
- [ ] Direktzugriff ohne gültige `orderId` zeigt Fehlerseite

---

### TICKET-6.12 – AccountOrdersPage + AccountOrderDetailPage

**Ziel:** Customer sieht alle seine Bestellungen im Kundenkonto.

**Dateien (neu):**
- `pages/account/AccountOrdersPage.tsx`
- `pages/account/AccountOrderDetailPage.tsx`

**Routen:**
- `/account/orders`
- `/account/orders/:orderId`

**Listenansicht:**
- Bestellnummer, Datum, Status-Badge, Gesamtpreis
- Link zum Detail

**Detailansicht:**
- Alle Items mit Bild, Name, Menge, Preis
- Liefer- und Rechnungsadresse
- Status der Gesamtbestellung
- Link zur Rechnung (`CustomerInvoice`)

**Akzeptanzkriterien:**
- [ ] Nur für eingeloggte CUSTOMER zugänglich
- [ ] API: `GET /api/customer/orders` und `GET /api/customer/orders/{id}`
- [ ] Leerer Zustand: „Du hast noch keine Bestellungen aufgegeben."
- [ ] `OrderStatusBadge`-Komponente wird verwendet

---

### TICKET-6.13 – SellerOrdersPage + Status-Update

**Ziel:** Seller sieht seine Teilbestellungen und kann den Status weiterschalten.

**Dateien (neu):**
- `pages/seller/SellerOrdersPage.tsx`
- `pages/seller/SellerOrderDetailPage.tsx`

**Routen:**
- `/seller/orders`
- `/seller/orders/:orderId`

**Statusübergänge per Button:**
- `CREATED` → „Bestellung bestätigen" → `CONFIRMED`
- `CONFIRMED` → „Vorbereitung starten" → `IN_PREPARATION`
- `IN_PREPARATION` → „An Lager verschickt" → `SHIPPED_TO_WAREHOUSE`

**Akzeptanzkriterien:**
- [ ] Nur für eingeloggte SELLER zugänglich
- [ ] API: `GET /api/seller/orders` und `PATCH /api/seller/orders/{id}/status`
- [ ] Statuswechsel-Button ist nur für den erlaubten nächsten Status sichtbar
- [ ] Ladestate und Fehlermeldung bei API-Fehler

---

### TICKET-6.14 – AdminOrdersPage + Status-Update

**Ziel:** Admin sieht alle Gesamtbestellungen und steuert den finalen Status.

**Datei (neu):** `pages/admin/AdminOrdersPage.tsx`

**Route:** `/admin/orders`

**Angezeigt:**
- Tabelle aller FulfillmentOrders
- Customer-ID, Datum, Status, Gesamtpreis
- Link zum Detail

**Detailansicht:**
- Alle SellerOrders mit ihrem Status
- Alle Items
- Rechnungsreferenz
- Button: Status auf `READY_FOR_FINAL_SHIPMENT` → nur aktiv wenn alle SellerOrders bereit
- Button: Status auf `COMPLETED`

**Akzeptanzkriterien:**
- [ ] Nur für eingeloggte ADMIN zugänglich
- [ ] API: `GET /api/admin/orders` und `PATCH /api/admin/orders/{id}/status`
- [ ] Status-Button für `READY_FOR_FINAL_SHIPMENT` deaktiviert wenn SellerOrders noch nicht bereit
- [ ] `OrderStatusBadge`-Komponente wird verwendet

---

## Gemeinsame Komponenten & Typen

### TICKET-6.15 – Typen & Shared Components (kein eigenes Ticket, aber Voraussetzung)

**Dateien (neu):**
- `types/order.ts`
  - `FulfillmentOrder`, `SellerOrder`, `OrderItem`, `CustomerInvoice`
  - `FulfillmentOrderStatus`, `SellerOrderStatus`
  - `CheckoutRequest`, `CheckoutResponse`
- `components/order/OrderStatusBadge.tsx`
  - Props: `status: FulfillmentOrderStatus | SellerOrderStatus`
  - Farbgebung je nach Status

---

## Reihenfolge der Umsetzung (empfohlen)

```
TICKET-6.1  → Modelle & Repositories
TICKET-6.7  → WarehouseProperties
TICKET-6.2  → CheckoutService
TICKET-6.3  → CheckoutController
TICKET-6.4  → CustomerOrderController
TICKET-6.5  → SellerOrderController
TICKET-6.6  → AdminOrderController

TICKET-6.8  → Auth-Guard Frontend
TICKET-6.9  → Adressformular
TICKET-6.10 → Bestellübersicht + Absenden
TICKET-6.11 → Bestätigungsseite
TICKET-6.12 → Kundenkonto Bestellungen
TICKET-6.13 → Seller Dashboard Orders
TICKET-6.14 → Admin Orders
```

---

## Definition of Done (Phase 6)

- [ ] Checkout nur mit eingeloggtem CUSTOMER möglich
- [ ] Bestellung wird korrekt im Backend persistiert
- [ ] Jeder Seller erhält seine eigene SellerOrder
- [ ] Admin sieht alle FulfillmentOrders
- [ ] Bestellbestätigung erscheint im Kundenkonto
- [ ] Warenkorb wird nach Checkout geleert
- [ ] Tests für `CheckoutService` vorhanden
- [ ] Tests für `CheckoutController` vorhanden

