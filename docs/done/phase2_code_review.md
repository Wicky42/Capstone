# Phase 2 – Seller Onboarding & Shop  
## Technische Zusammenfassung für Code Review

## 1. Ziel von Phase 2

Phase 2 baut die fachliche und technische Grundlage für den Seller-Bereich auf.  
Der Schwerpunkt lag auf:

- Seller-Onboarding
- Shop-Erstellung
- Shop-Verwaltung für den eigenen Seller
- sauberem Rollen-/Security-Zugriff
- erster Frontend-Anbindung für den Seller-Flow

Wichtig war dabei die fachliche Trennung:

- **Phase 2** erzeugt und verwaltet den Shop
- **Phase 3** vervollständigt das Onboarding fachlich mit dem ersten Produkt

Damit wurde bewusst verhindert, dass `onboardingCompleted` zu früh auf `true` springt.

---

## 2. Was in Phase 2 passiert ist

### Fachliche Leitidee
Das Seller-Onboarding wurde **nicht** als große State Machine mit persistiertem Status gebaut, sondern als **berechneter Status** aus echten Daten:

- Hat der Seller einen Shop?
- Sind die Shop-Daten vollständig?
- Gibt es bereits ein Produkt?
- Ist das Onboarding abgeschlossen?

Diese Entscheidung erhöht die Wartbarkeit, weil keine doppelte Wahrheit entsteht.

### Architekturentscheidung
Die Verantwortlichkeiten wurden sauber getrennt:

- **UserService**  
  liefert den aktuell eingeloggten User / Seller
- **SellerService**  
  enthält seller-spezifische Schreiblogik
- **ShopService**  
  enthält die Use Cases rund um Shop-Erstellung, Laden und Aktualisieren
- **SellerOnboardingService**  
  berechnet den Onboarding-Status rein lesend

### Seller-Flow
Der Seller wird nach Login nicht mehr auf einer allgemeinen Callback-Ansicht „geparkt“, sondern gezielt weitergeleitet zu:

- `/seller/onboarding`

Dort wird der Onboarding-Status geladen und der passende UI-Zustand angezeigt.

---

## 3. Welche Dateien angelegt oder angepasst wurden und warum

> Hinweis: Einige Dateien wurden neu angelegt, andere im Zuge des Flows angepasst oder vervollständigt.

## Backend

### `shop/model/Shop.java`
**Warum:**  
Eigenständige Shop-Domain als zentrales Objekt von Phase 2.

**Verantwortung:**  
Speichert Shop-Stammdaten wie Name, Beschreibung, Slug, Status und Zuordnung zum Seller.

---

### `shop/model/ShopStatus.java`
**Warum:**  
Trennung von Shop-Zustand und Onboarding-Schritt.

**Verantwortung:**  
Beschreibt den Shop-Zustand, z. B. `DRAFT`, `ACTIVE`, `INACTIVE`.

---

### `shop/repository/ShopRepository.java`
**Warum:**  
Fachliche Finder statt nur generisches CRUD.

**Wichtige Methoden:**
- `findBySellerId(...)`
- `existsBySellerId(...)`
- `findBySlug(...)`
- `existsByName(...)`

**Verantwortung:**  
Ermöglicht Seller-bezogene und shop-spezifische Datenzugriffe.

---

### `shop/dto/CreateShopRequest.java`
**Warum:**  
Klares API-Request-Objekt für die Shop-Erstellung.

**Verantwortung:**  
Enthält nur die vom Seller eingegebenen Felder:
- `name`
- `description`

Serverseitige Felder wie `sellerId`, `status`, `slug`, Timestamps bleiben bewusst draußen.

---

### `shop/dto/UpdateShopRequest.java`
**Warum:**  
Separates Update-DTO für erlaubte Änderungen am eigenen Shop.

**Verantwortung:**  
Definiert, welche Felder der Seller aktualisieren darf:
- `name`
- `description`
- `logoUrl`
- `headerImageUrl`

---

### `shop/dto/ShopResponse.java`
**Warum:**  
Saubere API-Antwort statt direktem Entity-Expose.

**Verantwortung:**  
Liefert den Shop in frontendfreundlicher Form zurück.  
Enthält eine `from(Shop shop)`-Methode für einfaches Mapping.

---

### `shop/service/ShopService.java`
**Warum:**  
Zentraler Shop-Use-Case-Service für Phase 2.

**Wichtige Aufgaben:**
- Shop für Seller erstellen
- eigenen Shop laden
- eigenen Shop aktualisieren
- Slug erzeugen
- Name-Eindeutigkeit prüfen
- `updatedAt` setzen

**Wichtige Methoden:**
- `createShopForSeller(...)`
- `getCurrentSellerShop()`
- `updateCurrentSellerShop(...)`
- interne Shop-Hilfsmethoden

---

### `seller/service/SellerService.java`
**Warum:**  
Seller-spezifische Änderungen gehören nicht in einen Mega-`UserService`.

**Wichtige Aufgaben:**
- Seller mit Shop verknüpfen
- seller-spezifische Änderungen speichern

**Wichtige Methode:**
- `linkShopToSeller(Seller seller, Shop shop)`

---

### `user/service/UserService.java`
**Warum:**  
Zentraler Zugriff auf den aktuell eingeloggten User / Seller.

**Wichtige Aufgaben:**
- aktuellen User aus dem Security Context bestimmen
- aktuellen User über OAuth2-Daten in Mongo auflösen
- Rolle prüfen
- aktuellen Seller zurückgeben

**Wichtige Methoden:**
- `getCurrentUser()`
- `getCurrentUserByRole(...)`
- `getCurrentSeller()`

---

### `seller/model/OnboardingStep.java`
**Warum:**  
Klare, typsichere Beschreibung der Onboarding-Schritte.

**Verantwortung:**  
Beschreibt den Fortschritt im Seller-Onboarding:
- `START`
- `SHOP_CREATION`
- `SHOP_CONFIGURATION`
- `PRODUCT_CREATION`
- `COMPLETED`

---

### `seller/dto/OnboardingStatusResponse.java`
**Warum:**  
Ein zentrales Response-Modell für das Frontend.

**Verantwortung:**  
Liefert den berechneten Status:
- `shopCreated`
- `shopDataComplete`
- `firstProductCreated`
- `onboardingCompleted`
- `currentStep`
- `nextStep`
- `message`

---

### `seller/service/SellerOnboardingService.java`
**Warum:**  
Onboarding-Status gehört in einen eigenen fachlichen Service.

**Verantwortung:**  
Berechnet den Status rein lesend.  
Keine Schreiblogik, keine Seiteneffekte.

**Wichtige Methode:**
- `getCurrentOnBoardingStatus()`

---

### `shop/controller/SellerShopController.java`
**Warum:**  
Seller-spezifische Shop-Endpunkte.

**Endpunkte:**
- `GET /api/seller/shops/my`
- `PUT /api/seller/shops/my`

**Verantwortung:**  
Zugriff auf den eigenen Shop des Sellers.

---

### `seller/controller/SellerOnboardingController.java`
**Warum:**  
Expliziter API-Endpunkt für den Onboarding-Status.

**Endpunkt:**
- `GET /api/seller/onboarding/status`

**Verantwortung:**  
Onboarding-Status für die Seller-UI bereitstellen.

---

### `security/SecurityConfig.java`
**Warum:**  
Absicherung der API und Definition der geschützten Bereiche.

**Wichtige Rolle in Phase 2:**
- `/api/seller/**` abgesichert
- Session-/OAuth2-Login bleibt zentral
- CORS/CSRF-Konfiguration für Frontend-Backend-Kommunikation

---

## Frontend

### `pages/seller/SellerOnboardingPage.tsx`
**Warum:**  
Zentrale Seller-Onboarding-Seite als eigener Einstiegspunkt.

**Verantwortung:**
- Onboarding-Status laden
- passende UI anzeigen
- nach Shop-Erstellung Status neu laden

---

### `components/seller/OnboardingStatusCard.tsx`
**Warum:**  
Saubere Anzeige des Onboarding-Fortschritts.

**Verantwortung:**
- aktueller Schritt
- nächster Schritt
- Message
- Übersicht der Status-Booleans

---

### `components/seller/CreateShopForm.tsx`
**Warum:**  
Seller kann direkt im Onboarding den Shop anlegen.

**Verantwortung:**
- Formular für Name/Beschreibung
- `POST /api/seller/shops`
- nach Erfolg Parent über `onSuccess()` informieren

---

### `components/seller/ShopOverviewCard.tsx`
**Warum:**  
Anzeige des vorhandenen Shops, sobald ein Shop existiert.

**Verantwortung:**
- `GET /api/seller/shops/my`
- Name, Beschreibung, Slug, Status anzeigen

---

### `services/sellerService.ts`
**Warum:**  
API-Zugriff für Seller-Onboarding kapseln.

**Verantwortung:**
- `GET /api/seller/onboarding/status`

---

### `services/shopService.ts`
**Warum:**  
Shop-Requests aus den Komponenten auslagern.

**Verantwortung:**
- `POST /api/seller/shops`
- `GET /api/seller/shops/my`
- `PUT /api/seller/shops/my`

---

### `types/Onboarding.ts`
**Warum:**  
Stabile Typisierung des Onboarding-Status im Frontend.

---

### `types/Shop.ts`
**Warum:**  
Stabile Typisierung der Shop-Antworten im Frontend.

---

### `pages/AuthCallbackPage.tsx`
**Warum angepasst:**  
Die Seite zeigt nicht mehr dauerhaft eine Willkommensansicht, sondern dient als technischer Zwischenschritt.

**Neue Verantwortung:**
- Login abschließen
- User laden / registrieren
- je nach Rolle weiterleiten:
  - `SELLER -> /seller/onboarding`
  - `CUSTOMER -> /`
  - `ADMIN -> eigener Zielpfad oder /`

---

### `App.tsx`
**Warum angepasst:**  
Seller-Onboarding wurde als echte Route ergänzt.

**Wichtige Ergänzung:**
- `/seller/onboarding`

---

## 4. Backend-Flow visualisiert

```text
Seller login via GitHub OAuth
        |
        v
Spring Security Session / OAuth2 principal
        |
        v
AuthCallbackPage (Frontend) ruft /api/auth/me bzw. register(...)
        |
        v
Redirect nach Rolle
        |
        +--> CUSTOMER -> /
        |
        +--> SELLER -> /seller/onboarding
                           |
                           v
                GET /api/seller/onboarding/status
                           |
                           v
                SellerOnboardingService
                           |
                           +--> UserService.getCurrentSeller()
                           |        |
                           |        v
                           |   OAuth2 principal lesen
                           |   User in Mongo laden
                           |   Rolle SELLER prüfen
                           |
                           v
                Shop vorhanden?
                    |
            +-------+--------+
            |                |
            | nein           | ja
            v                v
  currentStep=START   Shop-Daten prüfen
  nextStep=SHOP_CREATION     |
                              v
                     vollständig?
                        |
                +-------+--------+
                |                |
                | nein           | ja
                v                v
  currentStep=SHOP_CREATION   currentStep=SHOP_CONFIGURATION
  nextStep=SHOP_CONFIGURATION nextStep=PRODUCT_CREATION
```

---

## 5. Frontend-Flow visualisiert

```text
AuthCallbackPage
    |
    v
getMe() / register()
    |
    v
User.role bestimmen
    |
    +--> CUSTOMER -> navigate("/")
    |
    +--> SELLER -> navigate("/seller/onboarding")
                        |
                        v
              SellerOnboardingPage lädt Status
                        |
                        v
              GET /api/seller/onboarding/status
                        |
                        v
            State in SellerOnboardingPage setzen
                        |
                        +--> shopCreated = false
                        |        |
                        |        v
                        |   OnboardingStatusCard
                        |   CreateShopForm
                        |
                        +--> shopCreated = true
                                 |
                                 v
                            OnboardingStatusCard
                            ShopOverviewCard
```

---

## 6. Welche Herausforderungen gab es und wie wurden sie gelöst

## Herausforderung 1: Onboarding ist fachlich erst mit Produkt wirklich abgeschlossen
**Problem:**  
Phase 2 sollte Seller-Onboarding und Shop bauen, aber die Anforderungen sagen zugleich: Onboarding ist erst abgeschlossen, wenn mindestens ein Produkt existiert.

**Lösung:**  
`onboardingCompleted` wird in Phase 2 nicht vorschnell auf `true` gesetzt.  
Der Onboarding-Status wird stattdessen berechnet, und `firstProductCreated` bleibt der vorbereitete Hook für Phase 3.

---

## Herausforderung 2: Shop-Status und Onboarding-Schritt durften nicht vermischt werden
**Problem:**  
Ein Shop-Zustand (`DRAFT`, `ACTIVE`, `INACTIVE`) ist nicht dasselbe wie ein Onboarding-Schritt.

**Lösung:**  
Ein eigenes `ShopStatus`-Enum und ein eigenes `OnboardingStep`-Enum wurden getrennt eingeführt.

---

## Herausforderung 3: Verknüpfung Seller ↔ Shop sauber modellieren
**Problem:**  
Die Frage war, ob `linkShopToSeller(...)` auch selbst den Shop erzeugen soll.

**Lösung:**  
Die Verantwortlichkeiten wurden getrennt:
- `ShopService` erstellt den Shop
- `SellerService` verknüpft Seller und Shop

Das hält die Methoden klar und wartbar.

---

## Herausforderung 4: Sicherheits-/Principal-Zugriff im Backend
**Problem:**  
Der eingeloggte User wurde anfangs im Backend nicht korrekt erkannt.  
Die Prüfung `principal instanceof User` war bei OAuth2 falsch.

**Lösung:**  
Der aktuelle User wird nun über den OAuth2-Principal bestimmt.  
Aus dem Principal wird die GitHub-ID gelesen, und darüber wird der interne User in Mongo geladen:
- `oauthProvider = GITHUB`
- `oauthProviderUserId = GitHub-ID`

Dadurch funktioniert `getCurrentUser()` und darauf aufbauend `getCurrentSeller()` korrekt.

---

## Herausforderung 5: 403 im Seller-Onboarding
**Problem:**  
Das Frontend bekam beim Laden des Onboarding-Status einen 403.

**Ursache:**  
Nicht das Frontend, sondern der Backend-Zugriff auf den aktuellen User war fehlerhaft.

**Lösung:**  
Die Security-Frage wurde von der eigentlichen User-Auflösung getrennt:
- zuerst Security-Pfad geprüft
- dann User-Auflösung korrigiert
- danach funktionierte `/api/seller/onboarding/status`

---

## Herausforderung 6: Frontend-Route vs. Auth-Callback
**Problem:**  
Die Callback-Seite war zunächst gleichzeitig Auth-Abschluss und Zielseite.

**Lösung:**  
`AuthCallbackPage` wurde auf Redirect-Logik reduziert.  
Die eigentliche Seller-UI liegt jetzt auf `/seller/onboarding`.

---

## 7. Warum diese Struktur für Wartbarkeit sinnvoll ist

### Kleine Services statt God Object
Es wurde bewusst **kein Mega-UserService** gebaut.  
Stattdessen:

- `UserService` -> aktueller User / aktueller Seller
- `SellerService` -> seller-spezifische Änderungen
- `ShopService` -> Shop-Use-Cases
- `SellerOnboardingService` -> Onboarding-Berechnung

### DTOs statt direkte Entities
Das API-Interface ist klar getrennt:
- `CreateShopRequest`
- `UpdateShopRequest`
- `ShopResponse`
- `OnboardingStatusResponse`

### Frontend mit klarer Rollenführung
- Callback macht Auth
- Routing entscheidet nach Rolle
- Seller-Onboarding-Seite ist die echte Arbeitsoberfläche

---

## 8. Was in Phase 2 aus jetzigem Stand noch zu erledigen ist

## Backend
- Feinschliff / Review von Security-Authorities (`hasRole("SELLER")` sauber mit Authorities verheiraten)
- saubere Protected-Route-/Rollenstrategie finalisieren
- eventuell `ShopOverviewCard`-Updatefluss mit `PUT /api/seller/shops/my` komplett anbinden, falls noch nicht vollständig im UI

## Frontend
- `UpdateShopForm.tsx` ergänzen
- Bearbeiten des Shops sauber in den Seller-Flow integrieren
- Redirect-/Guard-Logik weiter festigen
- UX-Fehlerzustände und Ladezustände verfeinern

## Phase-3-Vorbereitung
- `SellerOnboardingService` um Produktprüfung erweitern
- `firstProductCreated`
- `onboardingCompleted = true` erst mit Produktlogik

---

## 9. Fazit

Phase 2 hat die technische und fachliche Basis für den Seller-Bereich geschaffen.  
Die zentralen Ergebnisse sind ein sauberer Onboarding-Status, ein klarer Seller-Shop-Flow, eine bessere Trennung von Authentifizierung und Fach-UI sowie eine wartbare Service-Struktur im Backend.  
Die wichtigsten Architekturentscheidungen waren die Trennung von Verantwortlichkeiten, die berechnete Onboarding-Logik statt eines persistierten Prozessstatus und die korrekte Auflösung des eingeloggten Users über den OAuth2-Principal.
