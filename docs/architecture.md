# Architecture

## Zweck

Dieses Dokument beschreibt die technische Architektur des Nischen-Marktplatzes für selbstgemachte Lebensmittel.
Es basiert auf den fachlichen Modellen in `user-model.md`, `shop-model.md`, `product-model.md`, `order-model.md` und `invoice-model.md` sowie den Anforderungen in `requirements.md`.

---

## Designentscheidungen (Übersicht)

| Thema | Entscheidung |
|---|---|
| Architekturstil | Modularer Monolith mit Layered Architecture |
| Auth | Sessionbasiert via Spring Security + GitHub OAuth2 |
| Bildupload | Lokaler Static-File-Server (`/static/images/`) |
| Warenkorb | Eigene Collection, wird nach Bestellung geleert |
| OrderItem | Eingebettetes Dokument (Snapshot-Prinzip) |
| Datenbank | MongoDB |
| Backend | Java 21 + Spring Boot |
| Frontend | React + TypeScript + Vite |

---

## Systemübersicht

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser (React)                       │
│              /customer/*  /seller/*  /admin/*               │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP / REST (JSON)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  Spring Boot Backend                         │
│                                                             │
│  Controller Layer  →  Service Layer  →  Repository Layer    │
│                                                             │
│  Spring Security (Session-based)                            │
│  GitHub OAuth2 Login                                        │
│  Static File Server (/static/images/)                       │
└────────────┬───────────────────────────┬────────────────────┘
             │                           │
             ▼                           ▼
    ┌─────────────────┐       ┌──────────────────────┐
    │    MongoDB       │       │  GitHub OAuth2 API   │
    │  (Datenbank)     │       │  (externer Provider) │
    └─────────────────┘       └──────────────────────┘
```

---

## Backend-Architektur

### Layered Architecture

Jede Domain folgt demselben Schichtaufbau:

```
controller/     →  HTTP-Endpunkte, Request/Response-Mapping, Rollenabsicherung
service/        →  Fachlogik, Validierung, Orchestrierung
repository/     →  Datenbankzugriff via Spring Data MongoDB
model/          →  Persistente Domänenentitäten (@Document)
dto/            →  Request- und Response-Objekte (kein direktes Exposé der Entitäten)
```

### Paketstruktur

```
org.example.backend
│
├── common/
│   ├── model/
│   │   └── Address.java               # gemeinsames Adressmodell
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   └── NotFoundException.java
│   └── validation/
│       └── ValidationUtils.java
│
├── security/
│   ├── SecurityConfig.java            # Spring Security + OAuth2 Konfiguration
│   ├── AuthController.java            # /api/auth/me, /api/auth/logout
│   ├── CustomOAuth2UserService.java   # GitHub-User → AppUser mapping
│   └── AppUserDetails.java
│
├── user/
│   ├── controller/UserController.java
│   ├── service/UserService.java
│   ├── repository/UserRepository.java
│   ├── model/AppUser.java
│   └── dto/
│       ├── UserResponse.java
│       └── RoleUpdateRequest.java
│
├── shop/
│   ├── controller/ShopController.java
│   ├── service/ShopService.java
│   ├── repository/ShopRepository.java
│   ├── model/Shop.java
│   └── dto/
│       ├── ShopRequest.java
│       └── ShopResponse.java
│
├── product/
│   ├── controller/ProductController.java
│   ├── service/ProductService.java
│   ├── repository/ProductRepository.java
│   ├── model/Product.java
│   └── dto/
│       ├── ProductRequest.java
│       └── ProductResponse.java
│
├── cart/
│   ├── controller/CartController.java
│   ├── service/CartService.java
│   ├── repository/CartRepository.java
│   ├── model/Cart.java
│   └── dto/
│       ├── CartItemRequest.java
│       └── CartResponse.java
│
├── order/
│   ├── controller/
│   │   ├── FulfillmentOrderController.java
│   │   └── SellerOrderController.java
│   ├── service/
│   │   ├── FulfillmentOrderService.java
│   │   └── SellerOrderService.java
│   ├── repository/
│   │   ├── FulfillmentOrderRepository.java
│   │   └── SellerOrderRepository.java
│   ├── model/
│   │   ├── FulfillmentOrder.java
│   │   ├── SellerOrder.java
│   │   └── OrderItem.java             # embedded document (Snapshot)
│   └── dto/
│       ├── CheckoutRequest.java
│       ├── FulfillmentOrderResponse.java
│       └── SellerOrderResponse.java
│
└── invoice/
    ├── controller/InvoiceController.java
    ├── service/
    │   ├── CustomerInvoiceService.java
    │   └── SellerSettlementService.java
    ├── repository/
    │   ├── CustomerInvoiceRepository.java
    │   └── SellerSettlementRepository.java
    ├── model/
    │   ├── CustomerInvoice.java
    │   └── SellerSettlement.java
    └── dto/
        ├── CustomerInvoiceResponse.java
        └── SellerSettlementResponse.java
```

---

## Sicherheitsarchitektur

### GitHub OAuth2 Login Flow

```
1. User klickt "Login mit GitHub" im Frontend
2. Frontend leitet auf /oauth2/authorization/github weiter
3. GitHub authentifiziert den User
4. Spring Security ruft CustomOAuth2UserService auf
5. Service prüft ob User bereits existiert (via oauthProviderUserId)
6. Neuer User → wird mit gewünschter Rolle (SELLER/CUSTOMER) angelegt
7. Bestehender User → Session wird wiederhergestellt
8. Session-Cookie wird gesetzt → User ist eingeloggt
```

### Rollenbasierte Absicherung

```java
// Beispiel
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('SELLER')")
@PreAuthorize("hasRole('CUSTOMER')")
@PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
```

### Security-Regeln

- `/api/auth/**` → öffentlich
- `/api/products/**` (GET) → öffentlich
- `/api/shops/**` (GET) → öffentlich
- `/api/admin/**` → nur `ADMIN`
- `/api/seller/**` → nur `SELLER`
- `/api/cart/**` → nur `CUSTOMER`
- `/api/orders/**` → rollenabhängig (je Endpunkt)
- `/static/**` → öffentlich (Bilder)

---

## MongoDB-Collections

### Übersicht

| Collection | Dokument | Hinweis |
|---|---|---|
| `users` | `AppUser` | Basis + rollenspezifische Felder |
| `shops` | `Shop` | gehört zu einem User (SELLER) |
| `products` | `Product` | gehört zu einem Shop |
| `carts` | `Cart` | ein Cart pro CUSTOMER |
| `fulfillment_orders` | `FulfillmentOrder` | mit embedded `OrderItem[]` |
| `seller_orders` | `SellerOrder` | mit embedded `OrderItem[]` |
| `customer_invoices` | `CustomerInvoice` | pro FulfillmentOrder |
| `seller_settlements` | `SellerSettlement` | monatlich pro Shop |

### Embedding vs. Referencing

| Struktur | Strategie | Begründung |
|---|---|---|
| `OrderItem` in `FulfillmentOrder` | **embedded** | Snapshot-Prinzip, keine späteren Produktänderungen dürfen Bestellung beeinflussen |
| `OrderItem` in `SellerOrder` | **embedded** | gleicher Grund |
| `Cart.items` | **embedded** | Warenkorb-Items sind temporär |
| `Shop` → `Product` | **referenced** (`shopId`) | Produkte werden unabhängig abgefragt |
| `User` → `Shop` | **referenced** (`shopId` im User) | 1:1-Beziehung, getrennte Verwaltung |
| `FulfillmentOrder` → `SellerOrder` | **referenced** (`sellerOrderIds`) | getrennte Sichtbarkeit je Rolle |
| `FulfillmentOrder` → `CustomerInvoice` | **referenced** (`invoiceId`) | Rechnung unabhängig ladbar |

### Cart-Lebenszyklus

- Ein `Cart` wird beim ersten Hinzufügen eines Produkts erstellt (falls noch nicht vorhanden)
- Nach Abschluss einer Bestellung (`Checkout`) wird der Cart **geleert** – das Cart-Dokument bleibt bestehen
- Pro Customer existiert immer genau ein Cart-Dokument

---

## API-Endpunkte

### Auth

| Methode | Pfad | Beschreibung | Zugriff |
|---|---|---|---|
| GET | `/api/auth/me` | eingeloggten User abrufen | authentifiziert |
| POST | `/api/auth/logout` | Session beenden | authentifiziert |
| GET | `/oauth2/authorization/github` | OAuth2 Login starten | öffentlich |

### User (Admin)

| Methode | Pfad | Beschreibung | Zugriff |
|---|---|---|---|
| GET | `/api/admin/users` | alle User auflisten | ADMIN |
| GET | `/api/admin/users/{id}` | User details | ADMIN |
| PUT | `/api/admin/users/{id}/role` | Rolle eines Users ändern | ADMIN |

### Shop

| Methode | Pfad | Beschreibung | Zugriff |
|---|---|---|---|
| GET | `/api/shops` | alle sichtbaren Shops | öffentlich |
| GET | `/api/shops/{id}` | Shop-Details | öffentlich |
| POST | `/api/seller/shops` | eigenen Shop erstellen | SELLER |
| PUT | `/api/seller/shops/{id}` | eigenen Shop bearbeiten | SELLER |
| GET | `/api/seller/shops/my` | eigenen Shop abrufen | SELLER |
| GET | `/api/admin/shops` | alle Shops | ADMIN |
| PUT | `/api/admin/shops/{id}/status` | Shop-Status ändern | ADMIN |

### Product

| Methode | Pfad | Beschreibung | Zugriff |
|---|---|---|---|
| GET | `/api/products` | alle sichtbaren Produkte (+ Filter/Suche) | öffentlich |
| GET | `/api/products/{id}` | Produktdetails | öffentlich |
| GET | `/api/shops/{shopId}/products` | Produkte eines Shops | öffentlich |
| POST | `/api/seller/products` | Produkt anlegen | SELLER |
| PUT | `/api/seller/products/{id}` | Produkt bearbeiten | SELLER |
| DELETE | `/api/seller/products/{id}` | Produkt löschen | SELLER |
| DELETE | `/api/admin/products/{id}` | Produkt systemweit entfernen | ADMIN |
| PUT | `/api/admin/products/{id}/recall` | Produktrückruf starten | ADMIN |

### Bildupload

| Methode | Pfad | Beschreibung | Zugriff |
|---|---|---|---|
| POST | `/api/images/upload` | Bild hochladen | SELLER |

Bilder werden lokal unter `src/main/resources/static/images/` gespeichert und sind unter `/static/images/{filename}` erreichbar.

### Cart

| Methode | Pfad | Beschreibung | Zugriff |
|---|---|---|---|
| GET | `/api/cart` | aktuellen Warenkorb abrufen | CUSTOMER |
| POST | `/api/cart/items` | Produkt hinzufügen | CUSTOMER |
| PUT | `/api/cart/items/{productId}` | Menge ändern | CUSTOMER |
| DELETE | `/api/cart/items/{productId}` | Produkt entfernen | CUSTOMER |
| DELETE | `/api/cart` | Warenkorb leeren | CUSTOMER |

### Order

| Methode | Pfad | Beschreibung | Zugriff |
|---|---|---|---|
| POST | `/api/orders/checkout` | Bestellung aus Warenkorb aufgeben | CUSTOMER |
| GET | `/api/orders/my` | eigene Bestellungen (FulfillmentOrders) | CUSTOMER |
| GET | `/api/orders/{id}` | Bestelldetails | CUSTOMER |
| GET | `/api/seller/orders` | eigene SellerOrders | SELLER |
| GET | `/api/seller/orders/{id}` | SellerOrder-Details | SELLER |
| PUT | `/api/seller/orders/{id}/status` | SellerOrder-Status aktualisieren | SELLER |
| GET | `/api/admin/orders` | alle FulfillmentOrders | ADMIN |
| GET | `/api/admin/orders/{id}` | vollständige Bestelldetails | ADMIN |

### Invoice

| Methode | Pfad | Beschreibung | Zugriff |
|---|---|---|---|
| GET | `/api/invoices/my` | eigene Rechnungen | CUSTOMER |
| GET | `/api/invoices/{id}` | Rechnungsdetails | CUSTOMER |
| GET | `/api/seller/settlements` | eigene monatliche Abrechnungen | SELLER |
| GET | `/api/admin/invoices` | alle Kundenrechnungen | ADMIN |
| GET | `/api/admin/settlements` | alle Seller-Abrechnungen | ADMIN |

---

## Frontend-Architektur

### Ordnerstruktur

```
src/
│
├── main.tsx                         # Entry Point
├── App.tsx                          # Router-Setup
│
├── types/                           # TypeScript-Interfaces
│   ├── User.ts
│   ├── Shop.ts
│   ├── Product.ts
│   ├── Cart.ts
│   ├── Order.ts
│   └── Invoice.ts
│
├── services/                        # Axios API-Calls
│   ├── authService.ts
│   ├── shopService.ts
│   ├── productService.ts
│   ├── cartService.ts
│   ├── orderService.ts
│   └── invoiceService.ts
│
├── hooks/                           # Custom React Hooks
│   ├── useAuth.ts
│   ├── useCart.ts
│   └── useProducts.ts
│
├── components/                      # Wiederverwendbare UI-Komponenten
│   ├── layout/
│   │   ├── Navbar.tsx
│   │   └── Footer.tsx
│   ├── product/
│   │   ├── ProductCard.tsx
│   │   └── ProductFilter.tsx
│   ├── shop/
│   │   └── ShopCard.tsx
│   ├── cart/
│   │   └── CartItem.tsx
│   └── shared/
│       ├── ProtectedRoute.tsx       # Rollenbasierter Routenschutz
│       └── LoadingSpinner.tsx
│
└── pages/
    ├── public/
    │   ├── HomePage.tsx             # Produktübersicht + Filter + Suche
    │   ├── ProductDetailPage.tsx
    │   └── ShopPage.tsx
    │
    ├── auth/
    │   └── RegisterPage.tsx         # Rollenauswahl vor OAuth-Login
    │
    ├── customer/
    │   ├── CartPage.tsx
    │   ├── CheckoutPage.tsx
    │   ├── OrdersPage.tsx
    │   └── InvoicePage.tsx
    │
    ├── seller/
    │   ├── OnboardingPage.tsx
    │   ├── ShopDashboardPage.tsx
    │   ├── ProductListPage.tsx
    │   ├── CreateProductPage.tsx
    │   ├── EditProductPage.tsx
    │   └── SellerOrdersPage.tsx
    │
    └── admin/
        ├── UserManagementPage.tsx
        ├── AdminOrdersPage.tsx
        └── AdminProductsPage.tsx
```

### Routing-Struktur

```
/                            → HomePage (öffentlich)
/product/:id                 → ProductDetailPage (öffentlich)
/shop/:id                    → ShopPage (öffentlich)
/register                    → RegisterPage (Rollenauswahl vor OAuth)

/customer/cart               → CartPage (CUSTOMER)
/customer/checkout           → CheckoutPage (CUSTOMER)
/customer/orders             → OrdersPage (CUSTOMER)
/customer/invoices/:id       → InvoicePage (CUSTOMER)

/seller/onboarding           → OnboardingPage (SELLER)
/seller/shop                 → ShopDashboardPage (SELLER)
/seller/products             → ProductListPage (SELLER)
/seller/products/create      → CreateProductPage (SELLER)
/seller/products/:id/edit    → EditProductPage (SELLER)
/seller/orders               → SellerOrdersPage (SELLER)

/admin/users                 → UserManagementPage (ADMIN)
/admin/orders                → AdminOrdersPage (ADMIN)
/admin/products              → AdminProductsPage (ADMIN)
```

---

## Checkout-Ablauf (Sequenz)

```
Customer klickt "Jetzt kaufen"
    │
    ▼
POST /api/orders/checkout  (mit shippingAddress, billingAddress)
    │
    ▼
FulfillmentOrderService
    ├── Cart wird abgerufen
    ├── FulfillmentOrder wird erstellt (isPaid = true)
    ├── SellerOrders werden pro Seller aufgeteilt
    ├── CustomerInvoice wird automatisch erstellt
    └── Cart wird geleert
    │
    ▼
Response: FulfillmentOrderResponse (mit invoiceId)
```

---

## Bildupload-Ablauf

```
Seller wählt Bild aus
    │
    ▼
POST /api/images/upload  (multipart/form-data)
    │
    ▼
Backend speichert Datei unter:
    src/main/resources/static/images/{uuid}_{filename}
    │
    ▼
Response: { "url": "/static/images/{uuid}_{filename}" }
    │
    ▼
URL wird als imageUrl im ProductRequest verwendet
```

---

## Fachliche Regeln in der Architektur

| Regel | Wo durchgesetzt |
|---|---|
| Ein Seller darf nur einen Shop erstellen | `ShopService` |
| Shop ohne Produkte ist nicht sichtbar | `ShopRepository` Query / `ShopService` |
| Onboarding muss abgeschlossen sein für Shop-Sichtbarkeit | `ShopService` |
| Seller darf nur eigene Produkte/Shop bearbeiten | `@PreAuthorize` + `SecurityContext` Check im Service |
| Customer sieht keine SellerOrders | `FulfillmentOrderController` gibt nur FulfillmentOrder zurück |
| Seller sieht keine FulfillmentOrders | `SellerOrderController` filtert nach `sellerId` |
| `isPaid = true` wird automatisch gesetzt | `FulfillmentOrderService.checkout()` |
| Rollenwechsel nur durch Admin | `UserController` mit `@PreAuthorize("hasRole('ADMIN')")` |

---

## Post-MVP-Erweiterbarkeit

| Feature | Vorbereitung im MVP |
|---|---|
| Echte Zahlungsabwicklung | `isPaid`-Feld + `PaymentStatus`-Enum bereits vorhanden; Service-Schicht erweiterbar |
| JWT statt Session | Spring Security Config austauschbar ohne Businesslogik-Änderung |
| Weitere OAuth-Provider | `CustomOAuth2UserService` unterstützt beliebige Provider via `oauthProvider`-Feld |
| Produktvarianten | `Product.category` als Array vorbereitet (`categories[]`) |
| Mehrere Zentrallager | `warehouseAddress` in `SellerOrder` bereits als Feld vorhanden |
| PDF-Rechnungen | `pdfUrl`-Feld in `CustomerInvoice` bereits vorgesehen |
| Seller-Abrechnungen | `SellerSettlement`-Modell bereits vollständig definiert |
| Monatliche Abrechnung | `SellerSettlementService` via `@Scheduled` erweiterbar |

