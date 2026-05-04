# Phase 3 – Produkte
## Technische Zusammenfassung für Code Review

## 1. Ziel von Phase 3

Phase 3 erweitert das System um die zentrale Produkt-Domain.

Der Schwerpunkt liegt auf:

- Produkt-Erstellung durch Seller
- Produkt-Bearbeitung durch Seller
- Produkt-Deaktivierung bzw. Löschen über Status
- Produktstatus und Produktlebenszyklus
- Validierung produktrelevanter Fachregeln
- Vorbereitung öffentlicher Produktkataloge
- Vorbereitung der Shop- und Seller-Katalogansicht
- Integration in das Seller-Onboarding
- saubere Frontend-Struktur für Produktseiten und Produktformulare

Wichtig ist dabei die fachliche Trennung:

- **Phase 2** hat Seller-Onboarding und Shop-Erstellung vorbereitet.
- **Phase 3** ergänzt die Produkte und vervollständigt damit den fachlichen Onboarding-Fortschritt.
- Ein Shop wird erst sinnvoll sichtbar, wenn mindestens ein Produkt existiert.

Phase 3 wurde bewusst nicht als reines CRUD-Feature umgesetzt, sondern als eigenständige Product-Domain mit klarer Ownership-, Validierungs- und Statuslogik.

---

## 2. Fachliche Leitidee

Ein Produkt ist die zentrale verkaufbare Einheit des Marktplatzes.

Jedes Produkt gehört:

- genau zu einem Seller
- genau zu einem Shop
- indirekt zur späteren Storefront
- später zu Warenkorbpositionen und Bestellungen

Auch wenn Seller und Shop aktuell eine 1:1-Beziehung haben, werden sowohl `sellerId` als auch `shopId` am Produkt gespeichert.

### Warum beide IDs?

`sellerId` dient vor allem der Seller-Verwaltung und Ownership-Prüfung:

```text
Zeige mir meine Produkte.
Seller darf nur eigene Produkte bearbeiten.
```

`shopId` dient vor allem dem Shop-Katalog:

```text
Zeige alle Produkte dieses Shops.
Shop ist öffentlich sichtbar, wenn Produkte existieren.
```

Dadurch bleibt der Code fachlich lesbar und spätere Erweiterungen werden einfacher.

---

## 3. Architekturentscheidung

Die bestehende modulare Layered Architecture wird fortgeführt.

Die Product-Domain folgt dem gleichen Aufbau wie die bestehenden Domains:

```text
product/
├── controller/
├── dto/
├── model/
├── repository/
└── service/
```

### Verantwortlichkeiten

| Schicht | Verantwortung |
|---|---|
| Controller | HTTP-Endpunkte, Request-Mapping, Rollenabsicherung |
| Service | Fachlogik, Ownership, Validierung, Statusregeln |
| Repository | MongoDB-Zugriff |
| Model | Persistente Produkt-Entity |
| DTO | API-Requests und API-Responses |

Wichtig: Entities werden nicht direkt über die API exposed. Controller und Frontend arbeiten mit DTOs.

---

## 4. Backend – erstellte oder angepasste Dateien

## 4.1 `product/model/Product.java`

### Zweck

Persistente Produkt-Entity für MongoDB.

### Verantwortung

Speichert alle produktrelevanten Daten:

- interne Produkt-ID
- Seller-Zuordnung
- Shop-Zuordnung
- Name
- Beschreibung
- Preis
- Kategorie
- Bild-URL
- Produktionsdatum
- Mindesthaltbarkeitsdatum
- Lagerbestand
- Produktstatus
- Erstellungs- und Änderungszeitpunkt

### Wichtige Felder

```java
private String id;
private String sellerId;
private String shopId;
private String name;
private String description;
private BigDecimal price;
private String category;
private String imageUrl;
private LocalDate productionDate;
private LocalDate bestBeforeDate;
private int stockQuantity;
private ProductStatus status;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
```

### Wichtige Entscheidung

Für Preise wird `BigDecimal` verwendet, nicht `double` oder `float`.

Grund:

- Geldbeträge dürfen keine Floating-Point-Rundungsfehler erzeugen.
- Preise werden im Service auf zwei Nachkommastellen normalisiert.

---

## 4.2 `product/model/ProductStatus.java`

### Zweck

Typsichere Abbildung des Produktstatus.

### Mögliche Statuswerte

```java
DRAFT
ACTIVE
INACTIVE
RECALLED
```

### Bedeutung

| Status | Bedeutung |
|---|---|
| `DRAFT` | Produkt ist angelegt, aber noch nicht öffentlich sichtbar |
| `ACTIVE` | Produkt ist aktiv und kann öffentlich angezeigt werden, sofern der Shop sichtbar ist |
| `INACTIVE` | Produkt wurde deaktiviert bzw. soft-deleted |
| `RECALLED` | Produkt wurde zurückgerufen und soll nicht normal kaufbar sein |

### Entscheidung

Es wird kein `visible`-Boolean gespeichert.

Die Sichtbarkeit wird fachlich berechnet aus:

```text
product.status == ACTIVE
AND shop.status == ACTIVE
AND seller/onboarding erfüllt
AND shop hat mindestens ein Produkt
```

---

## 4.3 `product/dto/CreateProductRequest.java`

### Zweck

Request-DTO für die Produkt-Erstellung.

### Verantwortung

Enthält nur Werte, die der Seller beim Erstellen eines Produkts eingeben darf.

Serverseitige Felder wie `sellerId`, `shopId`, `status`, `createdAt` und `updatedAt` werden nicht vom Client übernommen.

### Erwartete Felder

```java
String name;
String description;
BigDecimal price;
String category;
String imageUrl;
LocalDate productionDate;
LocalDate bestBeforeDate;
Integer stockQuantity;
```

### Validierung

Die Eingabevalidierung gehört in Request-DTOs, nicht in Response-DTOs.

Typische Regeln:

- Name darf nicht leer sein
- Beschreibung darf nicht leer sein
- Preis muss größer als `0` sein
- Preis darf maximal zwei Nachkommastellen haben
- Kategorie darf nicht leer sein
- Bild-URL darf nicht leer sein
- Produktionsdatum ist erforderlich
- Mindesthaltbarkeitsdatum ist erforderlich
- Bestand darf nicht negativ sein

---

## 4.4 `product/dto/UpdateProductRequest.java`

### Zweck

Request-DTO für Produkt-Bearbeitung.

### Entscheidung

Das Update-DTO ist bewusst partiell gehalten.

Nicht jedes Feld muss beim Update erneut übertragen werden. Dadurch sind PATCH-artige Updates möglich:

```json
{
  "price": 9.99
}
```

oder:

```json
{
  "stockQuantity": 0
}
```

### Erwartete Felder

```java
String name;
String description;
BigDecimal price;
String category;
String imageUrl;
LocalDate productionDate;
LocalDate bestBeforeDate;
Integer stockQuantity;
```

### Wichtige Regeln

- Null-Werte bedeuten: bestehender Wert bleibt erhalten.
- Gesetzte Werte werden validiert.
- `stockQuantity` darf nicht negativ sein.
- `price` muss größer als `0` sein.
- Die Datumsregel wird im Service geprüft, weil sie zwei Felder miteinander vergleicht.

---

## 4.5 `product/dto/ProductResponse.java`

### Zweck

API-Response für Produktdaten.

### Verantwortung

Gibt Produktdaten frontendfreundlich zurück.

### Wichtige Entscheidung

Validierungsannotationen gehören nicht in `ProductResponse`, da der Response keine Benutzereingabe validiert.

### Erwartete Felder

```java
String id;
String sellerId;
String shopId;
String name;
String description;
BigDecimal price;
String category;
String imageUrl;
LocalDate productionDate;
LocalDate bestBeforeDate;
Integer stockQuantity;
ProductStatus status;
```

### Mapping

Das Mapping erfolgt über:

```java
public static ProductResponse from(Product product)
```

Dadurch bleibt die Entity vom API-Vertrag getrennt.

---

## 4.6 `product/repository/ProductRepository.java`

### Zweck

MongoDB-Zugriff für Produkte.

### Aktuelle Methoden

```java
List<Product> findBySellerId(String sellerId);
List<Product> findByShopId(String shopId);
List<Product> findByStatus(ProductStatus status);
List<Product> findByShopIdAndStatus(String shopId, ProductStatus status);
boolean existsByShopId(String shopId);
boolean existsByIdAndSellerId(String productId, String sellerId);
```

### Warum diese Methoden?

#### `findBySellerId`

Für Seller-interne Produktverwaltung:

```text
Zeige alle Produkte des aktuellen Sellers.
```

#### `findByShopId`

Für Shop-Kataloge:

```text
Zeige alle Produkte dieses Shops.
```

#### `findByStatus`

Für spätere öffentliche Produktlisten:

```text
Zeige alle aktiven Produkte.
```

#### `findByShopIdAndStatus`

Für öffentliche Shop-Seiten:

```text
Zeige alle aktiven Produkte eines sichtbaren Shops.
```

#### `existsByShopId`

Für Onboarding- und Sichtbarkeitslogik:

```text
Hat der Shop mindestens ein Produkt?
```

#### `existsByIdAndSellerId`

Für Ownership- und Security-Prüfungen:

```text
Gehört dieses Produkt dem aktuellen Seller?
```

### Bewusste Entscheidung

Suche und Filter werden vorerst ins Frontend ausgelagert.

Deshalb enthält das Repository aktuell keine komplexen Textsuche- oder Regex-Queries.

---

## 4.7 `product/service/ProductService.java`

### Zweck

Zentrale Fachlogik für Produkt-Use-Cases.

### Verantwortung

Der Service enthält:

- Produkt-Erstellung für aktuellen Seller
- Produkt-Bearbeitung für aktuellen Seller
- Produkt-Deaktivierung bzw. Soft Delete
- Ownership-Prüfung
- Validierung fachlicher Regeln
- Preisnormalisierung
- Aktualisierung von Zeitstempeln
- Mapping zu `ProductResponse`

---

# 5. Backend Flow

## 5.1 Produkt erstellen

### Flow

```text
Seller ist eingeloggt
→ Frontend sendet CreateProductRequest
→ Controller nimmt Request entgegen
→ ProductService.createProductForCurrentSeller(...)
→ UserService liefert aktuellen Seller
→ ShopService liefert aktuellen Seller-Shop
→ Service validiert Produktdaten
→ Service normalisiert Preis
→ Product wird mit sellerId und shopId gebaut
→ Product startet mit ProductStatus.DRAFT
→ Product wird gespeichert
→ ProductResponse wird zurückgegeben
```

### Wichtige Regeln

- `sellerId` kommt nicht aus dem Request.
- `shopId` kommt nicht aus dem Request.
- `status` kommt nicht aus dem Request.
- Produkt startet mit `DRAFT`.
- `createdAt` und `updatedAt` werden serverseitig gesetzt.
- Preis wird mit `BigDecimal` verarbeitet und auf zwei Nachkommastellen normalisiert.

---

## 5.2 Produkt bearbeiten

### Flow

```text
Seller ist eingeloggt
→ Frontend sendet UpdateProductRequest
→ Controller ruft ProductService.updateProductForCurrentSeller(...)
→ ProductService lädt Produkt per ID
→ ProductService lädt aktuellen Seller
→ Ownership wird geprüft
→ gesetzte Request-Felder werden validiert
→ nur gesetzte Felder werden übernommen
→ createdAt bleibt unverändert
→ updatedAt wird aktualisiert
→ Product wird gespeichert
→ ProductResponse wird zurückgegeben
```

### Ownership-Regel

Ein Seller darf nur Produkte bearbeiten, die ihm gehören:

```text
product.sellerId == currentSeller.id
```

Wenn die IDs nicht übereinstimmen, wird der Zugriff verweigert.

---

## 5.3 Produkt deaktivieren / löschen

### Entscheidung

Produkte werden nicht hart gelöscht, sondern per Status deaktiviert.

```text
status = INACTIVE
```

### Warum Soft Delete?

Spätere Phasen führen Warenkorb, Bestellungen, Rechnungen und OrderItem-Snapshots ein.

Ein Hard Delete könnte später zu Problemen führen, wenn historische Daten auf Produkte verweisen.

---

## 5.4 Katalog nach Seller oder Shop laden

### Seller-Katalog

```text
GET /api/seller/products
→ ProductService lädt Produkte mit findBySellerId(currentSeller.id)
```

### Shop-Katalog

```text
GET /api/shops/{shopId}/products
→ ProductService lädt Produkte mit findByShopId oder findByShopIdAndStatus
```

Für interne Seller-Ansichten dürfen auch `DRAFT` oder `INACTIVE` Produkte sichtbar sein.

Für öffentliche Shop-Ansichten dürfen nur passende aktive Produkte angezeigt werden.

---

# 6. Validierungsregeln

## 6.1 Preis

### Entscheidung

`BigDecimal` wird für Preise verwendet.

### Regeln

- Preis darf nicht `null` sein beim Create.
- Preis muss größer als `0` sein.
- Preis wird auf zwei Nachkommastellen normalisiert.

Beispiel:

```text
8.999 → 9.00
```

---

## 6.2 Lagerbestand

### Regeln

- `stockQuantity` darf nicht negativ sein.
- Beim Create ist Bestand erforderlich.
- Beim Update wird Bestand nur geändert, wenn das Feld gesetzt ist.

Beispiel ungültig:

```json
{
  "stockQuantity": -1
}
```

---

## 6.3 Produktionsdatum und Mindesthaltbarkeitsdatum

### Regel

```text
bestBeforeDate >= productionDate
```

Das Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen.

### Warum im Service?

Die Regel vergleicht zwei Felder miteinander.

Bei Updates muss außerdem berücksichtigt werden, ob nur eines der beiden Felder geändert wird.

Beispiel:

```text
Bestehendes productionDate: 2026-04-01
Update bestBeforeDate:     2026-03-01
→ ungültig
```

---

# 7. Tests / TDD

## 7.1 ProductService Create Tests

Für `createProductForCurrentSeller(...)` wurden Tests vorgesehen für:

- Produkt wird für aktuellen Seller erstellt
- Produkt wird mit aktuellem Shop verknüpft
- Produkt startet mit `DRAFT`
- Preis wird normalisiert
- `createdAt` und `updatedAt` werden gesetzt
- ungültiges Mindesthaltbarkeitsdatum wird abgelehnt
- negativer Bestand wird abgelehnt
- Preis kleiner oder gleich 0 wird abgelehnt

---

## 7.2 ProductService Update Tests

Für `updateProductForCurrentSeller(...)` wurden Tests vorgesehen für:

- nur gesetzte Felder werden aktualisiert
- nicht gesetzte Felder bleiben unverändert
- Preis wird normalisiert
- `updatedAt` wird aktualisiert
- `createdAt` bleibt unverändert
- Produkt muss existieren
- fremdes Produkt darf nicht bearbeitet werden
- negativer Bestand wird abgelehnt
- Preis kleiner oder gleich 0 wird abgelehnt
- Datumsregel wird auch bei partiellen Updates geprüft

---

# 8. Frontend – neue Ordnerstruktur

Die Frontend-Struktur wurde für bessere Wartbarkeit und klare Verantwortlichkeiten neu geordnet.

```text
src/
├── components/
│   ├── layout/         ← Header.tsx + Header.css, Footer.tsx + Footer.css
│   ├── product/        ← ProductForm.tsx + ProductForm.css
│   └── seller/         ← alle Seller-Komponenten, jeweils mit CSS colocated
├── pages/
│   ├── auth/           ← AuthCallbackPage, LogoutPage, RegisterPage (+ CSS)
│   ├── product/        ← CreateProductPage
│   └── seller/         ← SellerOnboardingPage + SellerOnboardingPage.css
├── services/           ← api.ts, authService.ts, productService.ts, ...
├── styles/             ← nur globale CSS: globals, tokens, typography, ...
├── types/              ← address.ts, onboarding.ts, product.ts, seller.ts, shop.ts
└── utils/              ← getApiErrorMessages.ts
```

---

## 8.1 `components/layout/`

### Dateien

```text
Header.tsx
Header.css
Footer.tsx
Footer.css
```

### Zweck

Layout-Komponenten liegen zentral unter `components/layout`.

### Entscheidung

CSS für Layout-Komponenten ist colocated, also direkt neben der jeweiligen Komponente.

Dadurch bleibt nachvollziehbar, welche Styles zu welcher Komponente gehören.

---

## 8.2 `components/product/ProductForm.tsx`

### Zweck

Wiederverwendbares Formular für Produkt-Erstellung und später Produkt-Bearbeitung.

### Verantwortung

- Eingabefelder für Produktdaten anzeigen
- lokale Formularwerte verwalten
- grundlegende UI-Validierung ermöglichen
- Submit an Parent-Komponente delegieren

### Felder

- Produktname
- Beschreibung
- Preis
- Kategorie
- Bild-URL
- Produktionsdatum
- Mindesthaltbarkeitsdatum
- Bestand

### Entscheidung

`ProductForm` enthält möglichst wenig Businesslogik.

Die Seite entscheidet, ob ein Produkt erstellt oder bearbeitet wird.

---

## 8.3 `components/product/ProductForm.css`

### Zweck

Komponentenspezifisches Styling für das Produktformular.

### Entscheidung

Das Styling bleibt beim Formular, weil es nicht global wiederverwendet werden muss.

Globale Tokens, Farben und Typografie liegen weiterhin unter `styles/`.

---

## 8.4 `components/seller/`

### Zweck

Alle Seller-spezifischen Komponenten liegen unter `components/seller`.

### Entscheidung

Seller-Komponenten behalten ihre CSS-Dateien colocated.

Beispiel:

```text
OnboardingStatusCard.tsx
OnboardingStatusCard.css
CreateShopForm.tsx
CreateShopForm.css
ShopOverviewCard.tsx
ShopOverviewCard.css
```

Dadurch bleibt der Seller-Bereich modular und wartbar.

---

## 8.5 `pages/auth/`

### Dateien

```text
AuthCallbackPage.tsx
LogoutPage.tsx
RegisterPage.tsx
```

jeweils mit zugehörigem CSS, falls notwendig.

### Zweck

Alle Auth-Seiten werden unter `pages/auth` gebündelt.

Das trennt Auth-Flows klar von Seller-, Product- und Storefront-Seiten.

---

## 8.6 `pages/product/CreateProductPage.tsx`

### Zweck

Seite für das Erstellen eines Produkts.

### Flow

```text
Seller öffnet CreateProductPage
→ ProductForm wird angezeigt
→ Seller füllt Produktdaten aus
→ Seite ruft productService.createProduct(...)
→ Backend erstellt Produkt für aktuellen Seller und dessen Shop
→ Erfolgsfall: Weiterleitung oder Statusanzeige
→ Fehlerfall: Fehlermeldungen anzeigen
```

### Entscheidung

Die Seite orchestriert den Use Case.

Das Formular bleibt wiederverwendbar und kennt den konkreten API-Call nicht direkt.

---

## 8.7 `pages/seller/SellerOnboardingPage.tsx`

### Zweck

Zentrale Seller-Onboarding-Seite.

### Erweiterung in Phase 3

Die Seite berücksichtigt nun, dass ein vollständiges Onboarding nicht nur einen Shop, sondern auch mindestens ein Produkt benötigt.

### Flow

```text
Seller öffnet Onboarding-Seite
→ Onboarding-Status wird geladen
→ Wenn kein Shop existiert: Shop-Erstellung anzeigen
→ Wenn Shop existiert, aber kein Produkt: Produkt-Erstellung anbieten
→ Wenn Produkt existiert: Onboarding als abgeschlossen anzeigen
```

---

## 8.8 `services/productService.ts`

### Zweck

Kapselt alle API-Aufrufe rund um Produkte.

### Mögliche Methoden

```ts
createProduct(request)
updateProduct(productId, request)
getSellerProducts()
getShopProducts(shopId)
deleteProduct(productId)
```

### Entscheidung

Komponenten sprechen nicht direkt mit `fetch` oder `axios`.

API-Logik bleibt zentral im Service-Layer des Frontends.

---

## 8.9 `types/product.ts`

### Zweck

Zentrale TypeScript-Typen für Produkte.

### Mögliche Typen

```ts
export type ProductStatus = "DRAFT" | "ACTIVE" | "INACTIVE" | "RECALLED";

export type Product = {
  id: string;
  sellerId: string;
  shopId: string;
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl: string;
  productionDate: string;
  bestBeforeDate: string;
  stockQuantity: number;
  status: ProductStatus;
};

export type CreateProductRequest = {
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl: string;
  productionDate: string;
  bestBeforeDate: string;
  stockQuantity: number;
};
```

---

## 8.10 `utils/getApiErrorMessages.ts`

### Zweck

Zentrale Verarbeitung von API-Fehlern.

### Warum?

Backend-Validierungsfehler und Fachfehler sollen im Frontend einheitlich dargestellt werden.

Beispiel:

```text
Preis muss größer als 0 sein
Bestand darf nicht negativ sein
Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen
```

---

# 9. Frontend Flow

## 9.1 Produkt erstellen

```text
CreateProductPage
→ ProductForm
→ productService.createProduct(...)
→ POST /api/seller/products
→ Backend gibt ProductResponse zurück
→ UI zeigt Erfolg oder Fehler
```

---

## 9.2 Produkt im Onboarding erstellen

```text
SellerOnboardingPage
→ erkennt: Shop existiert, aber erstes Produkt fehlt
→ Link oder CTA zur CreateProductPage
→ Produkt wird erstellt
→ Onboarding-Status wird neu geladen
→ firstProductCreated = true
→ onboardingCompleted = true oder berechnet abgeschlossen
```

---

## 9.3 Fehleranzeige

```text
API liefert Fehler
→ productService wirft Fehler
→ Page fängt Fehler
→ getApiErrorMessages extrahiert lesbare Meldungen
→ UI zeigt Fehlermeldungen im Formular oder Alert
```

---

# 10. Security- und Ownership-Konzept

## 10.1 Rollenprüfung

Seller-Endpunkte sind nur für eingeloggte Seller zugänglich.

Beispiel:

```text
/api/seller/products/** → SELLER
```

## 10.2 Ownership-Prüfung

Rollenprüfung allein reicht nicht aus.

Ein Seller darf nur Produkte bearbeiten, die ihm gehören.

Deshalb prüft der Service:

```text
product.sellerId == currentSeller.id
```

Wenn diese Prüfung fehlschlägt, wird der Zugriff verweigert.

---

# 11. Wichtige Designentscheidungen

## 11.1 Keine Validierung im Response-DTO

Validierung gehört in Request-DTOs und Service-Logik.

`ProductResponse` ist nur ein Ausgabeformat.

---

## 11.2 Keine Web-Annotationen im Service

`@RequestBody`, `@PathVariable` und ähnliche HTTP-Annotationen gehören in Controller, nicht in Services.

Der Service bleibt frameworkärmer und besser testbar.

---

## 11.3 Preisnormalisierung im Service

Preise werden mit `BigDecimal` verarbeitet und auf zwei Nachkommastellen normalisiert.

---

## 11.4 Soft Delete statt Hard Delete

Produkte werden über Status deaktiviert.

Das schützt spätere Bestell- und Rechnungsdaten.

---

## 11.5 Suche zunächst im Frontend

Backend-Suche und komplexe Filterlogik werden bewusst nicht sofort eingebaut.

Für Phase 3 reicht es, Produktkataloge zu laden und Suche/Filter im Frontend anzuwenden.

---

# 12. Reviewer-Hinweise

Beim Review besonders prüfen:

- Wird `sellerId` immer serverseitig gesetzt?
- Wird `shopId` immer serverseitig aus dem aktuellen Seller-Shop ermittelt?
- Werden Produktdaten über DTOs übertragen und nicht über Entities?
- Wird `ProductResponse` ohne Validierungsannotationen gehalten?
- Wird `BigDecimal` korrekt verwendet?
- Wird der Preis auf zwei Nachkommastellen normalisiert?
- Wird negativer Bestand verhindert?
- Wird die Datumsregel korrekt geprüft?
- Kann ein Seller fremde Produkte nicht bearbeiten?
- Wird bei Updates nur geändert, was im Request gesetzt ist?
- Bleibt `createdAt` unverändert?
- Wird `updatedAt` aktualisiert?
- Wird kein Hard Delete durchgeführt?
- Ist das Frontend nach der neuen Struktur sortiert?
- Liegen globale Styles nur unter `styles/`?
- Liegen komponentenspezifische CSS-Dateien colocated bei den Komponenten?

---

# 13. Offene Punkte / mögliche Folge-Tickets

Diese Punkte sind bewusst nicht zwingend Teil der aktuellen Phase-3-Basis und können als Folgetickets behandelt werden:

- Produkt veröffentlichen: `DRAFT → ACTIVE`
- Produkt deaktivieren: `ACTIVE → INACTIVE`
- Produkt zurückrufen: `ACTIVE → RECALLED`
- Public Product List Page
- Product Detail Page
- Shop Product Catalog Page
- Backend-Pagination für Produktlisten
- Backend-Suche über MongoDB-Textindex
- Bild-Upload über eigene Upload-API
- Mehrere Produktbilder
- Produktvarianten
- Tags und erweiterte Filter
- Admin-Produktverwaltung

---

# 14. Zusammenfassung

Phase 3 führt die Product-Domain als eigenständigen, wartbaren Baustein ein.

Die wichtigsten Ergebnisse sind:

- Produktmodell mit Seller- und Shop-Zuordnung
- Produktstatus statt Boolean-Sichtbarkeit
- klare DTO-Trennung für Create, Update und Response
- schlankes Repository für Seller- und Shop-Kataloge
- ProductService mit Ownership, Validierung und Preisnormalisierung
- TDD-orientierte Tests für Create und Update
- Frontend-Struktur mit klarer Trennung nach `components`, `pages`, `services`, `types`, `styles` und `utils`
- Vorbereitung für Onboarding-Abschluss, Storefront, Cart und Orders

Die Phase schafft damit die Grundlage für die nächsten Roadmap-Schritte: Storefront, Warenkorb und Checkout.
