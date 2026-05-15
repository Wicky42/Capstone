# Phase 5 – Client-seitiger Warenkorb (Cart)

## Ziel

Vollständig client-seitiger Warenkorb ohne Backend und ohne Login.  
Der Warenkorb-State lebt in `localStorage` und wird global über einen `CartContext` bereitgestellt.  
Mengen werden durch `stockQuantity` des Produkts zum Zeitpunkt des Hinzufügens gedeckelt (Snapshot-Prinzip).  
In Phase 6 wird der Cart-Inhalt beim Checkout an das Backend übergeben.

---
cat >> /Users/viktoriauyanik/IdeaProjects/neueFischeBootcamp/capstone-project/docs/done/phase5-client-cart-review.md << 'EOF'

## Phase 5: Umsetzung

### TICKET-5.1 – Types anlegen ✅

**Datei:** `src/types/cart.ts`

`CartItem` mit allen Snapshot-Feldern (`productId`, `productName`, `unitPrice`, `quantity`, `maxQuantity`, `titleImage`, `shopId`, `sellerId`) und `Cart = { items: CartItem[] }` implementiert.

---

### TICKET-5.2 – `useCart`-Hook & `CartContext` ✅

**Dateien:** `src/hooks/useCart.ts`, `src/context/CartContext.ts`, `src/context/CartProvider.tsx`, `src/main.tsx`

- `useCart` kapselt die gesamte `localStorage`-Logik (`loadCart`/`saveCart` intern). State wird direkt via `useState(loadCart)` initialisiert — kein synchrones setState im Effect.
- `addItem` akzeptiert `Omit<CartItem, "quantity">` + optionale `quantity`; bei wiederholtem Hinzufügen wird Menge addiert und auf `maxQuantity` gedeckelt.
- `updateQuantity` und `removeItem` aktualisieren State und `localStorage` atomar via `setCart(prev => ...)`.
- `CartContext` trennt Typdefinition (`CartContext.ts`) von der Provider-Implementierung (`CartProvider.tsx`).
- `<CartProvider>` in `main.tsx` außen um `<App>` gewrapped.

---

### TICKET-5.3 – „In den Warenkorb"-Button auf der Produktdetailseite ✅

**Datei:** `src/pages/product/ProductDetailPage.tsx`

- Stepper (1 bis `stockQuantity`) direkt auf der Detailseite.
- `addItem()` aus `useCartContext()` befüllt alle Snapshot-Felder aus dem geladenen `Product`-Objekt.
- Button-Text wechselt nach Klick zu „✓ Hinzugefügt" für ~1,5 s, dann zurück zu „In den Warenkorb legen".
- Kein Login-Check — Cart ist öffentlich zugänglich.

---

### TICKET-5.4 – Header-Badge ✅

**Datei:** `src/components/layout/Header.tsx`

- Warenkorb-Icon (Tasche-SVG) links der Auth-Buttons in der Navbar.
- `{totalItems > 0 && <span className="header__cart-badge">...</span>}` — Badge zeigt Menge, bei > 99 wird `"99+"` angezeigt.
- Icon-Link führt zu `/cart`; `aria-label="Warenkorb"` für Accessibility.

---

### TICKET-5.5 – CartPage & CartItemRow ✅

**Dateien:** `src/pages/cart/CartPage.tsx`, `src/components/cart/CartItemRow.tsx`, `src/App.tsx`

#### CartItemRow
- Zeigt Produktbild, Name, Shop-Link, Einzelpreis, Stepper (+/–), Zeilenpreis, Entfernen-Button.
- „–" deaktiviert bei `quantity === 1`, „+" deaktiviert bei `quantity === maxQuantity`.

#### Verfügbarkeitsprüfung
- `CartPage` prüft beim Mount via `Promise.allSettled` den Status aller Items über `GET /api/public/products/{productId}`.
- Produkte mit Status `INACTIVE` / `RECALLED` oder 404-Fehler landen in `unavailableIds`.
- Betroffene Rows: ausgegraut, Badge „Nicht mehr verfügbar", Stepper deaktiviert.
- Hinweis in der Summary: „X Produkt(e) ist/sind nicht mehr verfügbar und wird beim Checkout entfernt."

#### CartPage
- **Leer-Zustand:** Tasche-SVG-Icon + „Dein Warenkorb ist leer" + Link „Zum Marktplatz".
- **Gefüllter Zustand:** Zweispaltig (Items links, Summary rechts): Liste aller `CartItemRow`-Einträge + Gesamtpreis (nur aktive Items) + deaktivierter „Zur Kasse →"-Button (Platzhalter Phase 6) + „← Weiter einkaufen"-Link.

#### Route
`<Route path="/cart" element={<CartPage />} />` in `App.tsx` eingetragen.

---

## Erreichter Gesamtstand

| Ticket | Status | Kernentscheidungen |
|---|---|---|
| 5.1 Types | ✅ | Snapshot-Prinzip: Felder eingefroren beim Hinzufügen |
| 5.2 Hook & Context | ✅ | `CartContext.ts` + `CartProvider.tsx` getrennt; Module-State in Hook |
| 5.3 Add-to-Cart | ✅ | Stepper + 1,5s-Feedback; kein Auth-Check |
| 5.4 Header-Badge | ✅ | `totalItems > 0` Bedingung; max. `"99+"` |
| 5.5 CartPage | ✅ | Live-Verfügbarkeitsprüfung via `Promise.allSettled`; Checkout-Button als Platzhalter für Phase 6 |


## Nachgelagerte Änderungen aus Phase 4: `ProductCategory`-Enum

### Warum

Bisher war `category` im `Product`-Modell ein freies `String`-Feld, was zu inkonsistenten DB-Werten, fehlender Validierung und einer hartkodiertem Kategorienliste im Frontend führte.

### Backend-Änderungen

| Datei | Änderung |
|---|---|
| `product/model/ProductCategory.java` | Neues Enum, 11 Werte je `name()` + `label`; `@JsonCreator fromValue()` |
| `product/model/Product.java` | `String category` → `ProductCategory category` |
| `product/dto/CreateProductRequest.java` | `String` → `ProductCategory`; `@NotBlank` entfernt |
| `product/dto/UpdateProductRequest.java` | `String` → `ProductCategory`; `@Size` entfernt |
| `product/dto/ProductResponse.java` | `String` → `ProductCategory` |
| `product/repository/ProductRepository.java` | Filter-Methoden: `String category` → `ProductCategory category` |
| `product/service/PublicProductService.java` | Parameter auf `ProductCategory`; keine `fromValue()`-Konvertierung mehr |
| `storefront/service/StorefrontService.java` | `String category` → `ProductCategory category` |
| `storefront/controller/StorefrontController.java` | `@RequestParam ProductCategory category` — Spring MVC konvertiert `?category=HONIG` via `Enum.valueOf()` |
| `storefront/controller/PublicCategoryController.java` | **Neu**: `GET /api/public/categories` → `[{ value, label }]` |

### Kategorie-Werte

| Enum-Name | Label |
|---|---|
| `HONIG` | Honig |
| `MARMELADE_KONFITUERE` | Marmelade & Konfitüre |
| `GEBAECK_KEKSE` | Gebäck & Kekse |
| `OELE_ESSIGE` | Öle & Essige |
| `AUFSTRICHE_PASTEN` | Aufstriche & Pasten |
| `GEWURZE_KRAEUTER` | Gewürze & Kräuter |
| `LIKOERE_SCHNAPSE` | Liköre & Schnäpse |
| `TEES_KRAEUTERMISCHUNGEN` | Tees & Kräutermischungen |
| `EINGEMACHTES_EINGELEGTES` | Eingemachtes & Eingelegtes |
| `OBST` | Obst |
| `SONSTIGES` | Sonstiges |

### Frontend-Änderungen (Kategorie)

| Datei | Änderung |
|---|---|
| `types/category.ts` | Statisches Array entfernt; nur `CategoryOption = { value, label }` und `ProductCategoryValue = string` |
| `types/product.ts` | Alle `category: string` → `category: ProductCategoryValue` |
| `services/storefrontService.ts` | `getCategories()` ergänzt |
| `hooks/useCategories.ts` | **Neu**: Modul-Level-Cache; Fallback bei Fehler |
| `components/base/CategoryFilterBar.tsx` | Nutzt `useCategories()` statt statischer Liste |
| `components/product/ProductForm.tsx` | Kategorie-Input → `<select>` mit dynamischen Optionen aus Backend |

### Hinweis: alter Datenbestand

Bestehende Produkte mit alten Label-Strings (z. B. `"Honig"`) in MongoDB können nicht deserialisiert werden — sie müssen neu angelegt werden. Seller sind durch das Dropdown gezwungen, eine gültige Kategorie zu wählen.

---