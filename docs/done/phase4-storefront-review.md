# Phase 4 – Storefront Review

## Ziel

Der öffentliche Marktplatz (Storefront) ermöglicht es jedem Besucher – ohne Login –, alle aktiven Produkte aller aktiven Shops zu entdecken, nach Produkten zu suchen, nach Kategorien zu filtern, Produkt- und Shop-Detailseiten aufzurufen sowie alle Shops zu durchstöbern.

---

## Backend-Umsetzung

### Architektur-Überblick

| Schicht | Klasse | Verantwortung |
|---|---|---|
| Controller | `StorefrontController` | Haupt-Einstiegspunkt für Discovery (Produkte & Shops) |
| Controller | `PublicProductController` | Detailseite & Bild eines einzelnen Produkts |
| Controller | `PublicShopController` | Detailseite eines Shops (per ID oder Slug) + Shop-Produkte |
| Service | `StorefrontService` | Orchestriert `PublicProductService` und `ShopService` |
| Service | `PublicProductService` | Öffentliche Produktabfragen mit Shop-Aktivitätsprüfung |
| Service | `ShopService` | Shop-Abfragen inkl. aktive Shops, Slug-Lookup |
| Repository | `ProductRepository` | MongoDB-Queries mit Shop- und Kategorie-Filtern |
| Repository | `ShopRepository` | MongoDB-Queries nach Status und Slug |

### Sicherheitsregel

Alle Endpunkte unter `/api/public/**` sind **ohne Authentifizierung** erreichbar. Intern stellen `PublicProductService` und `ShopService` sicher, dass **nur Produkte aus aktiven Shops** zurückgegeben werden (`isShopActive()` / `getActiveShopIds()`).

---

### Endpunkte

#### `StorefrontController` – `/api/public/storefront`

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/products` | Alle aktiven Produkte aller aktiven Shops. Optional: `?query=` (Suche), `?category=` (Filter), Pagination via `page`/`size` |
| `GET` | `/shops` | Alle aktiven Shops, paginiert |

#### `PublicProductController` – `/api/public/products`

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/{productId}` | Einzelnes aktives Produkt (404 wenn inaktiv oder Shop inaktiv) |
| `GET` | `/{productId}/image` | Produktbild als Binärdaten (`image/jpeg`, `image/png`, `image/webp`) |

#### `PublicShopController` – `/api/public/shops`

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/{shopId}` | Shop-Detailseite per ID (404 wenn nicht ACTIVE) |
| `GET` | `/by-slug/{slug}` | Shop-Detailseite per Slug – SEO-freundliche URL |
| `GET` | `/{shopId}/products` | Aktive Produkte eines bestimmten aktiven Shops, paginiert |

---

### Service-Verantwortlichkeiten

#### `PublicProductService`

- `findAllActiveProducts(category?, Pageable)` – filtert nach `ShopIdIn` aktiver Shops; optional nach Kategorie
- `searchActiveProducts(query, category?, Pageable)` – Namensuche kombinierbar mit Kategoriefilter
- `getActiveProductsByShop(shopId, Pageable)` – prüft Shop-Status, gibt Produkte des Shops zurück
- `getActiveProductById(id)` – prüft Produkt- und Shop-Status
- `getProductImage(id)` – wie `getActiveProductById`, gibt `ProductImage` zurück

#### `StorefrontService`

- `getProductView(query?, category?, Pageable)` – delegiert an `searchActiveProducts` oder `findAllActiveProducts`
- `getShopView(Pageable)` – delegiert an `shopService.getActiveShops()`

#### `ShopService` (Public-Erweiterungen)

- `getActiveShops(Pageable)` – alle Shops mit Status `ACTIVE`
- `getActiveShopById(id)` – wirft `ShopNotFoundException` wenn nicht ACTIVE
- `getActiveShopBySlug(slug)` – wie oben, per Slug-Lookup

---

### Repository-Methoden (relevante Ergänzungen)

| Repository | Methode | Zweck |
|---|---|---|
| `ProductRepository` | `findByStatusAndShopIdIn` | Storefront-Produktliste |
| `ProductRepository` | `findByNameContainingIgnoreCaseAndStatusAndShopIdIn` | Suche |
| `ProductRepository` | `findByCategoryAndStatusAndShopIdIn` | Kategoriefilter |
| `ProductRepository` | `findByNameContainingIgnoreCaseAndCategoryAndStatusAndShopIdIn` | Suche + Kategorie kombiniert |
| `ProductRepository` | `findByShopIdAndStatus(…, Pageable)` | Shop-Produktliste, paginiert |
| `ShopRepository` | `findByStatus(ShopStatus, Pageable)` | Aktive Shops paginiert |

---

## Frontend-Umsetzung

### Types

| Datei | Inhalt |
|---|---|
| `types/category.ts` | `PRODUCT_CATEGORIES`-Array, `ProductCategory`-Typ, `CATEGORY_LABELS`-Map – erweiterbar durch Anhängen |
| `types/product.ts` | `slug` zu `Product` ergänzt; `StorefrontSearchParams` hinzugefügt |

### Service

**`services/storefrontService.ts`** – nutzt `PageResponse<T>` aus `types/product.ts`

| Methode | Beschreibung |
|---|---|
| `getStorefrontProducts(params)` | Produkte mit query/category/page |
| `getStorefrontShops(page, size)` | Aktive Shops |
| `getProductById(id)` | Einzelprodukt |
| `getProductImageUrl(id)` | Gibt URL-String zurück (kein Fetch, direkt als `<img src>`) |
| `getShopBySlug(slug)` | Shop per Slug |
| `getShopById(id)` | Shop per ID |
| `getShopProducts(shopId, page, size)` | Produkte eines Shops |

### Komponenten

| Komponente | Pfad | Beschreibung |
|---|---|---|
| `ProductCard` | `components/product/` | Bild, Kategorie-Chip, Name, Preis, Shop-Link |
| `ProductCardSkeleton` | `components/product/` | Shimmer-Animation beim Laden |
| `ShopCard` | `components/seller/` | Logo-Placeholder (Anfangsbuchstabe), Name, Link per Slug |
| `ShopCardSkeleton` | `components/seller/` | Shimmer-Animation |
| `SearchBar` | `components/base/` | Input mit 300 ms Debounce |
| `CategoryFilterBar` | `components/base/` | Chips aus `PRODUCT_CATEGORIES`; Toggle-Verhalten |

### Seiten & Routing

| Seite | Route | Beschreibung |
|---|---|---|
| `StorefrontPage` | `/` | Tab Produkte/Shops; Suche + Filter; Skeleton-Grid; „Mehr laden" |
| `ProductDetailPage` | `/products/:productId` | Breadcrumb; Bild; Details; Shop-Link |
| `ShopDetailPage` | `/shops/:slug` | Header + Logo; Produkt-Grid mit „Mehr laden" |

---

### UX-Entscheidungen

| Entscheidung | Begründung |
|---|---|
| Slug-basierte URLs | SEO-freundlich; Slugs im Backend bereits vorhanden |
| „Mehr laden"-Button | Einfacher State; Produkte werden angehängt, nicht ersetzt |
| Skeleton-UI | Nutzer sieht sofort die Seitenstruktur → bessere _perceived performance_ |
| Kategorien als `const`-Array | Erweiterbar durch Anhängen; kein Backend-Call nötig |
| `getProductImageUrl()` als URL-String | Browser lädt Bild direkt – performanter als Axios-Fetch |