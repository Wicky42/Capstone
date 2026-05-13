# Phase 5 – Client-seitiger Warenkorb (Cart)

## Ziel

Vollständig client-seitiger Warenkorb im `localStorage`.  
Kein Backend, kein Login erforderlich.  
Ein schmaler `CartContext` hält den State global.  
Mengen werden durch `stockQuantity` des Produkts gedeckelt.  
In Phase 6 wird der Cart-Inhalt beim Checkout an das Backend übergeben.

---

## TICKET-5.1 – Types anlegen

**Datei:** `src/types/cart.ts`

### Aufgaben
- `CartItem` definieren mit Snapshot-Feldern:
    - `productId: string`
    - `productName: string`
    - `unitPrice: number` ← Preis zum Zeitpunkt des Hinzufügens
    - `quantity: number`
    - `maxQuantity: number` ← entspricht `stockQuantity` zum Zeitpunkt des Hinzufügens
    - `titleImage: string | null`
    - `shopId: string`
    - `sellerId: string`
- `Cart` definieren:
    - `items: CartItem[]`

### Hinweis (Snapshot-Prinzip)
Alle Felder werden beim Hinzufügen aus dem `Product`-Objekt befüllt und eingefroren.  
Spätere Preisänderungen durch den Seller verändern den bestehenden Cart-Eintrag **nicht**.

---

## TICKET-5.2 – `useCart`-Hook & `CartContext`

**Dateien:**
- `src/hooks/useCart.ts`
- `src/context/CartContext.tsx`
- `src/main.tsx` (Provider wrappen)

### Aufgaben

#### `useCart`-Hook (`localStorage`-Logik)
- `getCart(): Cart` → liest aus `localStorage` (Key: `"cart"`)
- `addItem(product: Product, quantity: number)` → fügt Item hinzu oder erhöht Menge; begrenzt auf `maxQuantity`
- `updateQuantity(productId: string, quantity: number)` → setzt Menge; Min: 1, Max: `maxQuantity`
- `removeItem(productId: string)` → entfernt einzelnes Item
- `clearCart()` → leert den gesamten Warenkorb

#### `CartContext`
- Stellt bereit: `items`, `totalItems` (Summe aller `quantity`), `addItem`, `updateQuantity`, `removeItem`, `clearCart`
- In `main.tsx` als `<CartProvider>` um `<App>` wrappen

### Fachliche Regeln
- Menge kann nie unter 1 fallen
- Menge kann nie über `maxQuantity` (= `stockQuantity` zum Zeitpunkt des Hinzufügens) steigen
- Bei erneutem `addItem` desselben Produkts: Menge addieren, aber Obergrenze beachten

---

## TICKET-5.3 – „In den Warenkorb"-Button auf der Produktdetailseite

**Datei:** `src/pages/product/ProductDetailPage.tsx`

### Aufgaben
- Mengenauswahl (Stepper: 1 bis `stockQuantity`) direkt auf der Detailseite einbauen
- `button-primary`-Button „In den Warenkorb legen" einfügen
- Bei Klick: `addItem()` aus `CartContext` aufrufen; alle Snapshot-Felder aus dem `Product`-Objekt befüllen
- Visuelles Feedback nach Klick: Button-Text wechselt kurz zu „✓ Hinzugefügt" (ca. 1,5 s), dann zurück

### Style-Hinweise (siehe `style.md`)
- Button: `.button-primary` (Eukalyptus `#84A98C`)
- Stepper: `.input`-Stil mit `min-height: 46px`, Border `#E7E0D6`
- Feedback-Farbe Erfolg: `#2F7D5C` auf `#E8F5EE`

### Kein Login-Check
Keine Authentifizierung notwendig – der Cart ist öffentlich zugänglich.

---

## TICKET-5.4 – Header-Badge

**Datei:** `src/components/layout/Header.tsx`

### Aufgaben
- Warenkorb-Icon (z.B. Tasche/Warenkorb-SVG) in der Navbar ergänzen
- Badge über dem Icon zeigt `totalItems` aus `CartContext`
- Badge nur sichtbar, wenn `totalItems > 0`
- Klick navigiert zu `/cart`

### Style-Hinweise
- Badge: `.badge` mit `background: var(--color-sand-100)`, `color: var(--color-sand-700)`
- Icon Button: min. `44×44px` (Accessibility-Regel)
- Positionierung: rechts in der Navbar, neben Login/Profil-Bereich

---

## TICKET-5.5 – CartPage & CartItemRow-Komponente

**Dateien:**
- `src/pages/cart/CartPage.tsx`
- `src/components/cart/CartItemRow.tsx`
- `src/App.tsx` (Route `/cart` registrieren)

### Aufgaben

#### `CartItemRow`-Komponente
- Zeigt: Produktbild (Snapshot), Produktname, Shop-Link, Einzelpreis, Mengensteuerung (+/–), Zeilenpreis, Entfernen-Button
- Mengensteuerung: „–" deaktiviert bei `quantity === 1`, „+" deaktiviert bei `quantity === maxQuantity`
- Entfernen-Button: ruft `removeItem()` auf

#### Inaktive / nicht mehr verfügbare Produkte
- Beim Rendern der `CartPage` wird zu jedem `CartItem` der aktuelle Produktstatus über die Storefront-API (`GET /api/public/products/{productId}`) geprüft
- Ist ein Produkt `INACTIVE` oder `RECALLED`:
    - Zeile ausgegraut anzeigen
    - Badge „Nicht mehr verfügbar" in `--color-warning-bg` / `--color-warning`
    - Menge nicht mehr änderbar
    - Hinweis: „Dieses Produkt kann nicht bestellt werden und wird beim Checkout entfernt."

#### `CartPage`
- Leer-Zustand: Illustration + Text „Dein Warenkorb ist noch leer" + Link zur Startseite
- Gefüllter Zustand: Liste aller `CartItemRow`-Einträge
- Gesamtpreisbereich (rechts oder unten): Summe aller aktiven Items (`quantity × unitPrice`)
- „Zur Kasse"-Button (`.button-primary`): **Platzhalter für Phase 6** – noch deaktiviert oder mit Tooltip „Kommt bald"
- „Weiter einkaufen"-Link zurück zur Startseite

#### Route
In `App.tsx` eintragen:
```tsx
<Route path="/cart" element={<CartPage />} />