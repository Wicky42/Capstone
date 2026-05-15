# Phase 6 – Checkout & Orders

## Ziel

Wenn ein Kunde auf „Zur Kasse" klickt, entsteht aus dem client-seitigen Warenkorb
eine vollständige, persistierte Bestellung im System.
Der Kunde muss dafür ein Kundenkonto besitzen.
Alle Beteiligten (Customer, Seller, Admin) sehen ihren relevanten Teil der Bestellung.

---

## Ziele von Phase 6

- Checkout nur für authentifizierte `CUSTOMER`
- Neukunden sollen beim Checkout ein Konto anlegen müssen
- Bestellung wird immer einem Customer zugeordnet
- Gesamtbestellung wird für Admin sichtbar
- Separate Teilbestellungen werden an die jeweiligen Händler erzeugt
- Bestellbestätigung ist im Kundenkonto auffindbar
- Warenkorb wird nach erfolgreichem Checkout geleert

---

## Voraussetzungen aus Phase 5

- Warenkorb lebt in `localStorage` über den `CartContext`
- „Zur Kasse"-Button auf der `CartPage` ist bereits vorbereitet
- Cart-Items enthalten Snapshot-Daten (`productId`, `productName`, `unitPrice`, `quantity`, `shopId`, `sellerId`, `titleImage`)
- Live-Verfügbarkeitsprüfung findet beim Öffnen der CartPage statt
- Checkout soll nur gültige und aktuelle Items weiterverarbeiten

---

## Fachliche Leitidee

Der Checkout erzeugt im System drei Ebenen:

1. **Customer-Sicht** → `FulfillmentOrder` + `CustomerInvoice`
2. **Seller-Sicht** → `SellerOrder` pro Händler / Shop
3. **Admin-Sicht** → vollständige Gesamtbestellung mit allen Teilbestellungen

Wichtig:
- Der Customer sieht nur seine eigene Gesamtbestellung
- Seller sehen nur ihre eigenen Bestellungen
- Admin sieht alles

---

## Checkout-Flow – Schritt für Schritt

### SCHRITT 1 – Checkout starten

**Auslöser:** Kunde klickt auf „Zur Kasse →" in der `CartPage`.

**Prüfungen:**
- Warenkorb ist nicht leer
- Alle Items sind verfügbar (`ACTIVE`)
- Nicht verfügbare Items werden vor dem Checkout entfernt
- Mindestens ein gültiges Item muss übrig bleiben

**Ergebnis:**
- Checkout-Prozess beginnt
- Weiterleitung zur `CheckoutPage`

---

### SCHRITT 2 – Authentifizierung / Customer-Konto

Der Checkout darf nur mit einem `CUSTOMER` abgeschlossen werden.

**Szenario A – Nicht eingeloggt:**
- Kunde wird zur `RegisterPage` / Login-Seite weitergeleitet
- Hinweis: „Um eine Bestellung aufzugeben, benötigst du ein Kundenkonto."
- Rolle wird auf `CUSTOMER` vorausgewählt
- Nach erfolgreichem GitHub OAuth-Login geht es zurück zur `CheckoutPage`
- Neuer `CUSTOMER`-User wird im Backend persistiert, falls noch nicht vorhanden

**Szenario B – Als SELLER eingeloggt:**
- Hinweis: „Seller-Konten können keine Bestellungen aufgeben."
- Option: Abmelden und mit CUSTOMER-Konto einloggen

**Szenario C – Als CUSTOMER eingeloggt:**
- Direkt zur `CheckoutPage` weiter

---

### SCHRITT 3 – Adressangabe

**Seite:** `CheckoutPage` – Schritt 1 von 2

**Felder:**
- Lieferadresse (`shippingAddress`)
  - Straße
  - Hausnummer
  - PLZ
  - Stadt
  - Land
- Rechnungsadresse (`billingAddress`)
  - Checkbox: „Gleiche Adresse wie Lieferadresse"
- Optional: gespeicherte Adresse aus dem Kundenprofil vorausfüllen

**Validierung:**
- Alle Pflichtfelder müssen ausgefüllt sein
- PLZ nur numerisch und 5-stellig für Deutschland
- Weiter-Button nur aktiv bei gültiger Adresse

---

### SCHRITT 4 – Bestellübersicht / Zusammenfassung

**Seite:** `CheckoutPage` – Schritt 2 von 2

**Angezeigt:**
- Alle Cart-Items mit Bild, Name, Shop, Einzelpreis, Menge, Zeilenpreis
- Gruppierung nach Shop / Seller
- Gesamtpreis aller aktiven Items
- Lieferadresse
- Rechnungsadresse
- Hinweis: „Bestellung gilt im MVP sofort als bezahlt (Vorkasse simuliert)"

**Aktionen:**
- „← Zurück" → Adresse ändern
- „Jetzt kaufen →" → Bestellung absenden

---

### SCHRITT 5 – Bestellung absenden (Frontend → Backend)

**API-Aufruf:**
```http
POST /api/orders/checkout
Authorization: Bearer <token>
```

**Request Body (`CheckoutRequest`):**
```json
{
  "items": [
	{
	  "productId": "...",
	  "productName": "...",
	  "unitPrice": 8.99,
	  "quantity": 2,
	  "titleImage": "...",
	  "shopId": "...",
	  "sellerId": "..."
	}
  ],
  "shippingAddress": {
	"street": "...",
	"houseNumber": "...",
	"postalCode": "...",
	"city": "...",
	"country": "..."
  },
  "billingAddress": {
	"street": "...",
	"houseNumber": "...",
	"postalCode": "...",
	"city": "...",
	"country": "..."
  }
}
```

---

### SCHRITT 6 – Backend: Atomare Order-Erstellung

**Verantwortlicher Service:** `CheckoutService`

**Grundidee:**
Die Bestellung wird in einem zusammenhängenden Backend-Flow erzeugt, damit keine Teildaten entstehen.

#### 6.1 Validierung
- `customerId` aus dem Security-Context lesen
- Prüfen, dass der eingeloggte User ein `CUSTOMER` ist
- Items dürfen nicht leer sein
- Jedes Item muss gültig sein:
  - `productId`
  - `shopId`
  - `sellerId`
  - `quantity > 0`
  - `unitPrice > 0`
- Produktstatus serverseitig erneut prüfen
- Nur `ACTIVE`-Produkte dürfen bestellt werden

#### 6.2 FulfillmentOrder erstellen
- Neue `FulfillmentOrder` mit Status `CREATED`
- `customerId` = eingeloggter Customer
- `items` = alle Cart-Items als `OrderItem`-Snapshots
- `totalPrice` = serverseitig berechnet
- `shippingAddress` als Snapshot speichern
- `billingAddress` als Snapshot speichern
- `isPaid = true` im MVP
- `createdAt = now()`

#### 6.3 SellerOrders splitten
- Items nach `sellerId` / `shopId` gruppieren
- Für jeden Seller eine eigene `SellerOrder` erzeugen
- Status der SellerOrder = `CREATED`
- `fulfillmentOrderId` setzen
- Nur die eigenen Items in der SellerOrder speichern
- `warehouseAddress` aus der Konfiguration setzen
- `createdAt = now()`
- `sellerOrderIds` in der `FulfillmentOrder` speichern

#### 6.4 CustomerInvoice erstellen
- Neue `CustomerInvoice` erstellen
- `fulfillmentOrderId`, `customerId`, `billingAddress` speichern
- `items` als Snapshot übernehmen
- `totalAmount` = Gesamtbetrag der FulfillmentOrder
- `invoiceNumber` generieren
- `createdAt = now()`
- `invoiceId` in der FulfillmentOrder speichern

#### 6.5 Customer-Profil aktualisieren
- Neue `orderId` zur Liste `customer.orderIds` hinzufügen
- Optional: Shipping- und Billing-Adresse im Kundenprofil speichern

#### 6.6 Antwort zurückgeben
```json
{
  "orderId": "fo-1001",
  "invoiceId": "inv-3001",
  "status": "CREATED",
  "totalPrice": 23.47
}
```

---

### SCHRITT 7 – Frontend: Cart leeren & Weiterleitung

Nach erfolgreichem `201 Created`:
- `CartContext.clearCart()` aufrufen
- `localStorage` wird geleert
- Weiterleitung zur Bestätigungsseite `/orders/{orderId}/confirmation`

---

### SCHRITT 8 – Bestellbestätigung im Kundenkonto

**Seite:** `OrderConfirmationPage` (`/orders/{orderId}/confirmation`)

**Angezeigt:**
- Erfolgsnachricht: „Deine Bestellung wurde erfolgreich aufgegeben!"
- Bestellnummer (`FulfillmentOrder.id`)
- Rechnungsnummer
- Bestellte Produkte
- Gesamtpreis
- Liefer- und Rechnungsadresse
- Hinweis: „Eine Bestellbestätigung findest du in deinem Kundenkonto unter ‚Meine Bestellungen'."
- Link zu `"/account/orders"`
- Link zu `"/"` zum Weiterkaufen

---

### SCHRITT 9 – Kundenkonto: Bestellübersicht

**Seite:** `AccountOrdersPage` (`/account/orders`)

**Login erforderlich:** `CUSTOMER`

**API-Aufruf:**
```http
GET /api/customer/orders
```

**Angezeigt pro Bestellung:**
- Bestellnummer
- Datum
- Status
- Gesamtpreis
- Liste der bestellten Produkte
- Link zur Bestelldetailseite

**Bestelldetailseite:** `/account/orders/{orderId}`
- Vollständige FulfillmentOrder-Daten
- Lieferadresse
- Rechnungsadresse
- Alle Items mit Snapshot-Daten
- Status der Gesamtbestellung
- Link zur Rechnung

---

### SCHRITT 10 – Seller: Neue Bestellung empfangen

**Seite:** `SellerOrdersPage` (`/seller/orders`)

**Login erforderlich:** `SELLER`

**API-Aufruf:**
```http
GET /api/seller/orders
```

**Angezeigt pro SellerOrder:**
- SellerOrder-ID
- Referenz auf die FulfillmentOrder-ID
- Datum
- Status
- Eigene Produkte mit Mengen und Einzelpreisen
- Lieferadresse des Zentrallagers

**Statusverlauf:**

| Von | Nach | Bedeutung |
|---|---|---|
| `CREATED` | `CONFIRMED` | Seller hat die Bestellung gesehen |
| `CONFIRMED` | `IN_PREPARATION` | Kommissionierung beginnt |
| `IN_PREPARATION` | `SHIPPED_TO_WAREHOUSE` | Sendung wurde ans Zentrallager verschickt |

```http
PATCH /api/seller/orders/{sellerOrderId}/status
```

---

### SCHRITT 11 – Admin: Gesamtbestellung einsehen

**Seite:** `AdminOrdersPage` (`/admin/orders`)

**Login erforderlich:** `ADMIN`

**API-Aufruf:**
```http
GET /api/admin/orders
```

**Angezeigt:**
- Alle FulfillmentOrders
- Customer-Referenz
- Gesamtpreis
- Status
- Datum
- Alle SellerOrders
- Alle Items
- Rechnungsreferenz

**Admin kann den Gesamtstatus steuern:**
- `PROCESSING` → `READY_FOR_FINAL_SHIPMENT`
- `READY_FOR_FINAL_SHIPMENT` → `COMPLETED`

---

## Neue API-Endpunkte in Phase 6

| Methode | Pfad | Rolle | Beschreibung |
|---|---|---|---|
| `POST` | `/api/orders/checkout` | CUSTOMER | Bestellung aufgeben |
| `GET` | `/api/customer/orders` | CUSTOMER | Eigene Bestellungen auflisten |
| `GET` | `/api/customer/orders/{id}` | CUSTOMER | Bestelldetail abrufen |
| `GET` | `/api/customer/invoices/{id}` | CUSTOMER | Rechnung abrufen |
| `GET` | `/api/seller/orders` | SELLER | Eigene SellerOrders auflisten |
| `GET` | `/api/seller/orders/{id}` | SELLER | SellerOrder-Detail |
| `PATCH` | `/api/seller/orders/{id}/status` | SELLER | Bestellstatus ändern |
| `GET` | `/api/admin/orders` | ADMIN | Alle FulfillmentOrders |
| `GET` | `/api/admin/orders/{id}` | ADMIN | FulfillmentOrder-Detail |
| `PATCH` | `/api/admin/orders/{id}/status` | ADMIN | FulfillmentOrder-Status ändern |

---

## Neue Backend-Klassen für Phase 6

### Package: `order`

| Klasse | Typ | Beschreibung |
|---|---|---|
| `FulfillmentOrder` | Document | Gesamtbestellung des Customers |
| `SellerOrder` | Document | Seller-spezifische Teilbestellung |
| `OrderItem` | Embedded | Produkt-Snapshot innerhalb einer Bestellung |
| `CustomerInvoice` | Document | Rechnung für den Customer |
| `CheckoutRequest` | DTO | Request-Body beim Checkout |
| `CheckoutResponse` | DTO | Antwort nach erfolgreichem Checkout |
| `FulfillmentOrderResponse` | DTO | Lesbare Gesamtbestellung fürs Frontend |
| `SellerOrderResponse` | DTO | Lesbare SellerOrder fürs Frontend |
| `CheckoutService` | Service | Validierung, Split, Invoice-Erstellung |
| `CustomerOrderService` | Service | Lesen für Customer |
| `SellerOrderService` | Service | Lesen + Status-Update für Seller |
| `AdminOrderService` | Service | Lesen + Status-Update für Admin |
| `CheckoutController` | Controller | `POST /api/orders/checkout` |
| `CustomerOrderController` | Controller | Customer-Endpunkte |
| `SellerOrderController` | Controller | Seller-Endpunkte |
| `AdminOrderController` | Controller | Admin-Endpunkte |
| `FulfillmentOrderRepository` | Repository | MongoDB |
| `SellerOrderRepository` | Repository | MongoDB |
| `CustomerInvoiceRepository` | Repository | MongoDB |
| `WarehouseProperties` | Config | Zentrallager-Adresse aus `application.properties` |

---

## Neue Frontend-Komponenten / Seiten in Phase 6

| Datei | Beschreibung |
|---|---|
| `pages/checkout/CheckoutPage.tsx` | Mehrstufiger Checkout (Adresse + Übersicht) |
| `pages/checkout/OrderConfirmationPage.tsx` | Bestätigungsseite nach erfolgreichem Kauf |
| `pages/account/AccountOrdersPage.tsx` | Bestellübersicht im Kundenkonto |
| `pages/account/AccountOrderDetailPage.tsx` | Detailansicht einer Bestellung |
| `pages/seller/SellerOrdersPage.tsx` | Bestellübersicht für Seller |
| `pages/seller/SellerOrderDetailPage.tsx` | Detailansicht einer SellerOrder |
| `pages/admin/AdminOrdersPage.tsx` | Gesamtbestellübersicht für Admin |
| `components/checkout/AddressForm.tsx` | Wiederverwendbares Adressformular |
| `components/checkout/OrderSummary.tsx` | Zusammenfassung vor dem Kaufabschluss |
| `components/order/OrderStatusBadge.tsx` | Status-Badge für Orders |
| `services/orderService.ts` | API-Calls für Orders |
| `types/order.ts` | TypeScript-Typen für Order, SellerOrder, Invoice |

---

## Tickets für Phase 6

### Backend
- **TICKET-6.1** – `FulfillmentOrder`, `SellerOrder`, `OrderItem`, `CustomerInvoice` als Models & Repositories
- **TICKET-6.2** – `CheckoutService`: Validierung, FulfillmentOrder erstellen, SellerOrders splitten, Invoice erzeugen
- **TICKET-6.3** – `CheckoutController`: `POST /api/orders/checkout`
- **TICKET-6.4** – `CustomerOrderController`: Bestellhistorie & Rechnungen für Customer
- **TICKET-6.5** – `SellerOrderController`: SellerOrders lesen + Status-Update
- **TICKET-6.6** – `AdminOrderController`: Gesamtübersicht + Status-Update
- **TICKET-6.7** – `WarehouseProperties`: Zentrallager-Adresse aus `application.properties`

### Frontend
- **TICKET-6.8** – Auth-Guard auf Checkout: Redirect zu Login/Register wenn nicht eingeloggt
- **TICKET-6.9** – `CheckoutPage` Schritt 1: Adressformular
- **TICKET-6.10** – `CheckoutPage` Schritt 2: Bestellübersicht + API-Call + Cart leeren
- **TICKET-6.11** – `OrderConfirmationPage`
- **TICKET-6.12** – `AccountOrdersPage` + `AccountOrderDetailPage` im Kundenkonto
- **TICKET-6.13** – `SellerOrdersPage` + Status-Update im Seller Dashboard
- **TICKET-6.14** – `AdminOrdersPage` + Status-Update im Admin Panel

---

## MVP-Einschränkungen in Phase 6

- Keine echte Zahlung – `isPaid = true` direkt beim Checkout
- Kein Storno durch Customer
- Kein Teilablehnen durch Seller
- Genau ein Zentrallager
- Keine E-Mail-Benachrichtigungen
- Keine PDF-Rechnung im MVP

---

## Offene Entscheidungen

| # | Frage | Option A | Option B |
|---|---|---|---|
| 1 | Adresse im Kundenprofil speichern? | Immer überschreiben | Nur beim ersten Checkout |
| 2 | Checkout-Struktur im Frontend? | Eine Seite mit Stepper | Separate Routen `/checkout/address` + `/checkout/review` |
| 3 | FulfillmentOrder → `PROCESSING` wann? | Sofort bei Erstellung | Wenn erste SellerOrder `CONFIRMED` |

---

## Ergebnis von Phase 6

Nach Phase 6 kann das System:

- einen Checkout nur mit Kundenkonto durchführen
- die Bestellung fachlich sauber splitten
- Bestellungen Customer, Seller und Admin korrekt zuordnen
- eine Bestellbestätigung im Kundenkonto anzeigen
- die Grundlage für Versand und spätere Rechnungs-/Zahlungslogik bereitstellen
