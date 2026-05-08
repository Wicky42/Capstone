# Phase 3.5 – Seller Dashboard
## Technische Zusammenfassung für Code Review

## 1. Ziel von Phase 3.5

Phase 3.5 schließt den Übergang zwischen der Produkt-Domain aus Phase 3 und der öffentlichen Storefront aus Phase 4.

Der Schwerpunkt lag auf dem Aufbau eines ersten **Seller Dashboards**, damit Seller nach abgeschlossenem Onboarding nicht mehr nur im Onboarding-Flow landen, sondern ihre Produkte und später ihren Shop operativ verwalten können.

Phase 3.5 ist bewusst als Zwischenschritt umgesetzt worden:

- **Phase 3** hat die Product-Domain aufgebaut.
- **Phase 3.5** macht diese Product-Domain für Seller verwaltbar.
- **Phase 4** kann danach auf einer klaren öffentlichen Sichtbarkeitslogik aufbauen.

Wichtig ist dabei die fachliche Trennung:

- Seller sehen und verwalten ihre eigenen Produkte unabhängig vom öffentlichen Status.
- Customers sollen später nur Produkte sehen, die öffentlich sichtbar sind.
- Shop-Freigabe erfolgt nicht automatisch durch das Onboarding, sondern durch einen Admin.

---

## 2. Fachliche Leitidee

Das Seller Dashboard ist kein öffentlicher Shop und keine Storefront.

Es ist ein interner Arbeitsbereich für Seller.

Ein Seller soll dort:

- seine Produkte sehen
- Produkte bearbeiten
- Produkte veröffentlichen
- Produkte abhängig vom Status deaktivieren oder reaktivieren
- später Shopdaten bearbeiten
- den Status seines Shops nachvollziehen können

Die öffentliche Sichtbarkeit bleibt davon getrennt.

Ein Produkt kann im Seller Dashboard sichtbar und bearbeitbar sein, auch wenn es öffentlich noch nicht sichtbar ist.

Das ist wichtig, weil Produkte unterschiedliche Status besitzen können:

- `DRAFT`
- `ACTIVE`
- `INACTIVE`
- `RECALLED`

Außerdem hängt öffentliche Sichtbarkeit nicht nur vom Produktstatus ab, sondern auch vom Shopstatus und der Admin-Freigabe.

---

## 3. Zentrale fachliche Entscheidung: Admin-Freigabe des Shops

Eine wichtige Ergänzung in Phase 3.5 ist die Entscheidung, dass ein Shop nach abgeschlossenem Onboarding **nicht automatisch aktiv** wird.

Der Ablauf ist fachlich wie folgt:

```text
Seller erstellt Shop
→ Seller legt mindestens ein Produkt an
→ Onboarding ist fachlich abgeschlossen
→ Admin erhält bzw. soll eine Benachrichtigung erhalten
→ Admin prüft den Shop
→ Admin gibt den Shop frei bzw. aktiviert ihn
→ Erst dann kann der Shop öffentlich sichtbar werden
```

Die geplante Admin-Benachrichtigung lautet sinngemäß:

```text
Händler hat Onboarding abgeschlossen, bitte verifiziere seinen Shop.
```

Dadurch bleibt die Plattformqualität kontrollierbar.

Ein Seller darf seinen Shop nicht selbst freischalten.

### Erlaubte Shop-Statusänderungen durch Seller

Ein Seller soll nur folgende Statusänderung selbst auslösen können:

```text
ACTIVE → INACTIVE
```

Das bedeutet:

- Ein Seller kann seinen freigegebenen Shop pausieren oder deaktivieren.
- Ein Seller kann seinen Shop nicht selbst von `DRAFT` auf `ACTIVE` setzen.
- Ein Seller kann seinen Shop nicht selbst erneut aktivieren, wenn eine Admin-Prüfung notwendig ist.

### Erlaubte Shop-Statusänderungen durch Admin

Der Admin ist für die Freigabe verantwortlich.

Fachlich relevant sind insbesondere:

```text
DRAFT → ACTIVE
INACTIVE → ACTIVE
```

Für spätere Erweiterungen könnte ein eigener Status wie `SUSPENDED` sinnvoll sein, wenn ein Shop durch die Plattform gesperrt wird. Für den aktuellen MVP-Stand reicht die bestehende Statuslogik jedoch aus.

---

## 4. Was in Phase 3.5 passiert ist

## 4.1 Seller Dashboard wurde eingeführt

Es wurde eine erste Dashboard-Struktur für Seller ergänzt.

Relevante Commit-Hinweise:

```text
add frontend - CreateProductPage.tsx - add Seller Dashboard - add Route to App.tsx
export DashboardStats in own component
```

Das Dashboard dient als neuer Einstiegspunkt für Seller nach abgeschlossenem Onboarding.

Es bündelt die Produktverwaltung und bereitet spätere Shopverwaltungsfunktionen vor.

---

## 4.2 Login-Redirect für Seller wurde angepasst

Der Seller-Login unterscheidet nun zwischen abgeschlossenem und nicht abgeschlossenem Onboarding.

Relevanter Commit-Hinweis:

```text
Check if Seller Onboarding is completed, if so navigate to seller/dashboard after login, else navigate to seller/onboarding
```

Der neue Flow lautet:

```text
SELLER Login
→ Onboarding-Status prüfen
→ wenn Onboarding nicht abgeschlossen ist: /seller/onboarding
→ wenn Onboarding abgeschlossen ist: /seller/dashboard
```

Damit bleibt das Onboarding weiterhin der richtige Ort für neue Seller.

Seller, die ihr Onboarding bereits abgeschlossen haben, werden aber nicht mehr unnötig zurück in den Onboarding-Flow geschickt.

---

## 4.3 ProductListPage für Seller wurde ergänzt

Seller können nun ihre eigenen Produkte in einer Produktliste sehen.

Relevanter Commit-Hinweis:

```text
Add ProductListPage -> Seller is able to see all products and CTA edit, publish
```

Die ProductListPage ist Teil des Seller Dashboards und zeigt Produkte aus Seller-Sicht.

Wichtig: Diese Liste darf nicht nur aktive Produkte enthalten.

Ein Seller muss auch Produkte sehen können, die sich in folgenden Status befinden:

- `DRAFT`
- `ACTIVE`
- `INACTIVE`
- `RECALLED`

Nur so kann der Seller Produkte bearbeiten, veröffentlichen oder den Status nachvollziehen.

---

## 4.4 Produktaktionen abhängig vom Produktstatus

Die Buttons in der Produktliste wurden abhängig vom aktuellen Produktstatus angepasst.

Relevante Commit-Hinweise:

```text
change buttons depending on product status
add product recalled status in frontend
```

Dadurch wird verhindert, dass alle Produkte unabhängig vom Status dieselben Aktionen anbieten.

Fachlich sinnvoll ist folgende Logik:

### `DRAFT`

```text
Bearbeiten
Veröffentlichen
```

### `ACTIVE`

```text
Bearbeiten
Deaktivieren
```

### `INACTIVE`

```text
Bearbeiten
Wieder veröffentlichen
```

### `RECALLED`

```text
Keine normale Aktivierung durch Seller
```

Der Status `RECALLED` ist fachlich besonders sensibel.

Ein zurückgerufenes Produkt darf nicht einfach durch den Seller wieder veröffentlicht werden.

---

## 4.5 Activate Endpoint wurde ergänzt

Für die Veröffentlichung bzw. Aktivierung von Produkten wurde ein eigener Endpoint ergänzt.

Relevanter Commit-Hinweis:

```text
add /activate endpoint
```

Außerdem wurde die Service-Namensgebung angepasst:

```text
change updateProduct -> activateProduct in service
```

Das ist eine gute Entscheidung, weil das Veröffentlichen eines Produkts fachlich kein normales Update ist.

Ein expliziter Use Case ist wartbarer als eine versteckte Statusänderung innerhalb eines generischen `updateProduct`.

Der fachliche Use Case lautet:

```text
Seller veröffentlicht eigenes Produkt
→ System prüft Ownership
→ System prüft Produktstatus
→ System setzt Status auf ACTIVE
```

Wichtig bleibt:

- Seller dürfen nur eigene Produkte aktivieren.
- `RECALLED` Produkte dürfen nicht regulär durch Seller aktiviert werden.
- Die Aktivierung eines Produkts bedeutet nicht automatisch, dass das Produkt öffentlich sichtbar ist.

Öffentliche Sichtbarkeit hängt zusätzlich vom Shopstatus und von der Admin-Freigabe ab.

---

## 4.6 GET Mapping mit Status-Filter wurde ergänzt

Es wurde ein GET Mapping ergänzt, das nach Status filtern kann.

Relevante Commit-Hinweise:

```text
GET Mapping with status filter able
GET Mapping with status filter able
```

Das ist hilfreich für verschiedene Produktansichten:

- alle Seller-Produkte
- nur aktive Produkte
- nur Entwürfe
- nur inaktive Produkte
- später öffentliche Produktlisten

Wichtig ist jedoch die Trennung:

Ein Statusfilter allein ersetzt nicht die öffentliche Sichtbarkeitsprüfung.

Für die spätere Storefront reicht also nicht:

```text
status == ACTIVE
```

Für öffentliche Produktlisten muss zusätzlich geprüft werden:

```text
product.status == ACTIVE
AND shop.status == ACTIVE
AND seller onboarding completed
AND shop was approved by admin
```

---

## 4.7 Seller-spezifisches Laden von Produkten wurde ergänzt

Für das Bearbeiten von Produkten im Seller Dashboard wurde ergänzt, dass Seller Produkte laden können, die nicht nur aktiv sind.

Relevanter Commit-Hinweis:

```text
add getSellerProductById to get Products that are not only active (for seller dashboard, editing products)
```

Das ist fachlich wichtig.

Ein öffentlicher Produkt-Endpunkt darf später nur sichtbare Produkte liefern.

Ein Seller-Endpunkt muss dagegen auch eigene nicht-öffentliche Produkte liefern, zum Beispiel:

- Entwürfe
- inaktive Produkte
- Produkte in Bearbeitung
- zurückgerufene Produkte zur Einsicht

Damit bleibt die Trennung zwischen Public Product API und Seller Product API sauber.

---

## 4.8 EditProductPage wurde ergänzt

Für das Seller Dashboard wurde eine EditProductPage angelegt.

Relevante Commit-Hinweise:

```text
frontend : add EditProductPage for seller dashboard
frontend : add Routes for EditProductPage and SellerShopEditPage
```

Die EditProductPage ermöglicht es Sellern, vorhandene Produkte zu bearbeiten.

Wichtig ist hierbei:

- Produktdaten werden über einen Seller-spezifischen Endpunkt geladen.
- Es dürfen nicht nur aktive Produkte geladen werden.
- Die Ownership-Prüfung muss im Backend stattfinden.
- Das Frontend darf nicht entscheiden, ob ein Seller ein Produkt bearbeiten darf.

---

## 4.9 Routen für EditProductPage und SellerShopEditPage wurden ergänzt

Es wurden neue Routen für die Produktbearbeitung und die spätere Shopbearbeitung ergänzt.

Relevanter Commit-Hinweis:

```text
frontend : add Routes for EditProductPage and SellerShopEditPage
```

Damit ist die technische Navigation vorbereitet für:

```text
/seller/products/:productId/edit
/seller/shop/edit
```

Die SellerShopEditPage ist aktuell angelegt, aber noch nicht vollständig gebaut.

---

## 5. SellerShopEditPage – aktueller Stand und geplante Verantwortung

Die `SellerShopEditPage` wurde bereits als Route bzw. Datei vorbereitet, ist aber noch nicht vollständig umgesetzt.

Sie soll später die Shopverwaltung für Seller abbilden.

Der Seller soll dort folgende Dinge ändern können:

- Name bzw. Shopname
- Adresse
- ggf. Beschreibung und weitere Shop-Stammdaten
- Shop deaktivieren

Wichtig ist die Einschränkung:

Der Seller darf den Shop nicht selbst aktivieren.

Die Aktivierung bzw. Freigabe bleibt Aufgabe des Admins.

### Geplante erlaubte Seller-Aktion

```text
ACTIVE → INACTIVE
```

### Nicht erlaubte Seller-Aktionen

```text
DRAFT → ACTIVE
INACTIVE → ACTIVE
```

Wenn ein Seller relevante Shopdaten ändert, sollte später fachlich geprüft werden, ob eine erneute Admin-Freigabe notwendig ist.

Für den aktuellen Stand kann diese Regel zunächst dokumentiert und in einer späteren Admin-/Shop-Phase konkretisiert werden.

---

## 6. Aktuelle technische Struktur

## Frontend

In Phase 3.5 wurden insbesondere folgende Bereiche erweitert:

```text
seller dashboard routing
seller dashboard page
seller product list
seller product edit page
seller shop edit route
product status actions
product status badges / status handling
product service methods
```

Die wichtigsten neuen oder angepassten Seiten sind:

```text
SellerDashboardPage
ProductListPage
EditProductPage
SellerShopEditPage
CreateProductPage
```

Die wichtigsten Frontend-Funktionen sind:

```text
Seller nach Onboarding zum Dashboard leiten
Seller-Produkte anzeigen
Produkt abhängig vom Status bearbeiten/veröffentlichen/deaktivieren
Produkte laden, auch wenn sie nicht öffentlich aktiv sind
```

---

## Backend

Backend-seitig wurden insbesondere Produkt-Endpunkte und Product-Service-Methoden erweitert.

Wichtige Ergänzungen:

```text
GET Mapping mit optionalem Statusfilter
Seller-spezifisches Laden eines Produkts nach ID
/activate Endpoint
activateProduct statt generischem updateProduct für Aktivierung
```

Die fachliche Richtung ist korrekt:

- Statusänderungen werden als eigene Use Cases behandelt.
- Seller-Kontext und öffentliche Produktanzeige bleiben getrennt.
- Seller können auch Produkte verwalten, die öffentlich nicht sichtbar sind.

---

## 7. Wichtige Clean-Code-Entscheidungen

## 7.1 Explizite Use Cases statt versteckter Statusupdates

Die Umbenennung bzw. Trennung von `updateProduct` und `activateProduct` ist sinnvoll.

Ein Produkt zu aktivieren ist fachlich etwas anderes als Produktdaten zu bearbeiten.

Dadurch wird der Code lesbarer:

```text
updateProduct
→ Produktdaten ändern

activateProduct
→ Produkt veröffentlichen / Statuswechsel auslösen
```

---

## 7.2 Seller API und Public API getrennt denken

Ein häufiger Fehler wäre, denselben GET-Endpunkt für Seller Dashboard und öffentliche Storefront zu verwenden.

Das sollte vermieden werden.

Seller Dashboard:

```text
zeigt eigene Produkte in allen relevanten Status
```

Public Storefront:

```text
zeigt nur öffentlich sichtbare Produkte
```

Diese Trennung ist für spätere Phasen wichtig, insbesondere für:

- Storefront
- Cart
- Checkout
- Orders
- Admin-Prüfungen
- Produktrückrufe

---

## 7.3 Statuslogik nicht ins Frontend verlagern

Das Frontend darf Buttons abhängig vom Status anzeigen oder ausblenden.

Die eigentliche Berechtigung und fachliche Validierung muss aber im Backend bleiben.

Das bedeutet:

- Frontend verbessert UX.
- Backend schützt die Fachregeln.

Beispiel:

```text
Frontend blendet "Veröffentlichen" bei RECALLED aus.
Backend verhindert trotzdem RECALLED → ACTIVE.
```

---

## 7.4 Shop-Aktivierung bleibt Admin-Verantwortung

Die Shop-Aktivierung darf nicht indirekt durch Seller-Onboarding passieren.

Das Onboarding beantwortet nur:

```text
Hat der Seller die notwendigen Schritte erledigt?
```

Die Admin-Freigabe beantwortet:

```text
Darf dieser Shop öffentlich auf der Plattform erscheinen?
```

Das sind zwei unterschiedliche fachliche Konzepte.

---

## 8. Offene Punkte nach Phase 3.5

## 8.1 SellerShopEditPage fertigstellen

Die Seite ist vorbereitet, aber noch nicht vollständig gebaut.

Noch umzusetzen:

- Shopdaten laden
- Formular für Shopname
- Formular für Adresse
- ggf. Beschreibung bearbeiten
- Shop deaktivieren
- Speichern-Flow
- Fehler- und Loading-States
- Validierung

Wichtig:

Der Seller darf den Shop nicht selbst aktivieren.

---

## 8.2 Admin-Benachrichtigung vorbereiten

Es fehlt noch die technische Umsetzung der Benachrichtigung an Admins, sobald ein Seller das Onboarding abgeschlossen hat.

Fachlich gewünschtes Verhalten:

```text
Seller schließt Onboarding ab
→ System erkennt prüfbereiten Shop
→ Admin erhält Benachrichtigung
→ Admin prüft und aktiviert Shop
```

Mögliche technische Varianten:

### Variante A: Pending-Verification-Liste

Für den MVP reicht eventuell zunächst ein Admin-Endpunkt:

```text
GET /api/admin/shops/pending-verification
```

Dieser listet Shops, bei denen gilt:

```text
onboardingCompleted == true
AND shop.status == DRAFT
AND shop has at least one product
```

### Variante B: Notification Collection

Später könnte eine eigene Notification-Collection ergänzt werden:

```json
{
  "id": "notification-123",
  "type": "SELLER_ONBOARDING_COMPLETED",
  "shopId": "shop-123",
  "sellerId": "seller-123",
  "message": "Händler hat Onboarding abgeschlossen, bitte verifiziere seinen Shop.",
  "read": false,
  "createdAt": "..."
}
```

Für den aktuellen MVP ist Variante A wahrscheinlich einfacher und ausreichend.

---

## 8.3 Admin-Freigabe-Endpunkt ergänzen

Für eine spätere Admin-Phase wird ein Endpunkt benötigt, mit dem Admins Shops aktivieren können.

Möglicher Endpoint:

```text
PUT /api/admin/shops/{shopId}/activate
```

Regeln:

- nur `ADMIN`
- Shop muss existieren
- Seller-Onboarding muss abgeschlossen sein
- Shop muss mindestens ein Produkt haben
- Shopstatus wird auf `ACTIVE` gesetzt

Optional:

```text
PUT /api/admin/shops/{shopId}/deactivate
```

---

## 8.4 Öffentliche Sichtbarkeit in Phase 4 zentralisieren

Für Phase 4 muss die Sichtbarkeitsregel zentral im Backend umgesetzt werden.

Ein Produkt ist öffentlich sichtbar, wenn:

```text
product.status == ACTIVE
AND shop.status == ACTIVE
AND seller onboarding completed
AND shop was approved by admin
```

Da die Admin-Freigabe aktuell über `shop.status == ACTIVE` abgebildet wird, darf ein öffentliches Produkt niemals nur über `product.status == ACTIVE` ermittelt werden.

---

## 9. Empfohlene nächste Tickets

## Ticket 3.5-F1 – SellerShopEditPage fertigstellen

### Ziel

Seller können ihre eigenen Shop-Stammdaten bearbeiten und ihren aktiven Shop deaktivieren.

### Aufgaben

- Shopdaten laden
- Formular bauen
- Name bearbeiten
- Adresse bearbeiten
- Beschreibung bearbeiten, falls im Modell vorgesehen
- Save-Button
- Loading- und Error-State
- Deaktivieren-Button nur anzeigen, wenn Shop `ACTIVE` ist

### Akzeptanzkriterien

- Seller kann eigenen Shop bearbeiten
- Seller kann keinen fremden Shop bearbeiten
- Seller kann Shop nicht selbst aktivieren
- Seller kann aktiven Shop auf `INACTIVE` setzen

---

## Ticket 3.5-F2 – Seller Shop Deactivation Endpoint

### Ziel

Seller können ihren aktiven Shop deaktivieren.

### Endpoint

```text
PUT /api/seller/shops/my/deactivate
```

### Regeln

- nur `SELLER`
- nur eigener Shop
- erlaubt nur `ACTIVE → INACTIVE`
- kein `DRAFT → ACTIVE`
- kein `INACTIVE → ACTIVE`

---

## Ticket 3.5-F3 – Admin Pending Shop Verification vorbereiten

### Ziel

Admins können erkennen, welche Shops nach abgeschlossenem Onboarding geprüft werden müssen.

### Endpoint-Vorschlag

```text
GET /api/admin/shops/pending-verification
```

### Kriterien für pending verification

```text
Seller onboarding completed
AND Shop status == DRAFT
AND Shop has at least one product
```

---

## Ticket 3.5-F4 – Admin Shop Activation vorbereiten

### Ziel

Admin kann einen geprüften Shop freigeben.

### Endpoint-Vorschlag

```text
PUT /api/admin/shops/{shopId}/activate
```

### Regeln

- nur `ADMIN`
- Shop muss existieren
- Seller muss Onboarding abgeschlossen haben
- Shop muss mindestens ein Produkt haben
- Shopstatus wird `ACTIVE`

---

## Ticket 3.5-F5 – Dashboard Hinweis für ausstehende Shop-Freigabe

### Ziel

Seller verstehen, warum ihr Shop trotz abgeschlossenem Onboarding noch nicht öffentlich sichtbar ist.

### Anzeige

```text
Dein Onboarding ist abgeschlossen.
Dein Shop wartet auf Freigabe durch einen Admin.
```

### Akzeptanzkriterien

- Hinweis erscheint, wenn Onboarding abgeschlossen ist, Shop aber noch nicht `ACTIVE` ist
- Hinweis verschwindet, wenn Shop aktiv ist
- Keine falsche öffentliche Sichtbarkeit wird suggeriert

---

## 10. Definition of Done für Phase 3.5

Phase 3.5 gilt als abgeschlossen, wenn:

- Seller nach abgeschlossenem Onboarding zum Dashboard gelangen
- Seller bei unvollständigem Onboarding weiterhin zum Onboarding gelangen
- Seller ihre eigenen Produkte im Dashboard sehen
- Seller Produkte bearbeiten können
- Seller Produkte veröffentlichen können
- Seller Produktaktionen abhängig vom Produktstatus sehen
- `RECALLED` im Frontend berücksichtigt wird
- Seller-Produkte auch geladen werden können, wenn sie nicht öffentlich aktiv sind
- ein eigener Activate-Use-Case existiert
- Statusfilter für Produktlisten vorbereitet sind
- Routen für Produktbearbeitung und Shopbearbeitung existieren
- klar dokumentiert ist, dass Shop-Aktivierung durch Admin erfolgt
- klar dokumentiert ist, dass Seller Shops nur deaktivieren, aber nicht selbst aktivieren dürfen

---

## 11. Zusammenfassung

Phase 3.5 hat aus der reinen Product-Domain eine nutzbare Seller-Verwaltung gemacht.

Der wichtigste Fortschritt ist, dass Seller nun nach abgeschlossenem Onboarding in ein Dashboard wechseln und dort ihre Produkte verwalten können.

Technisch wurden dafür Routen, Pages, Statusaktionen, ein Activate-Endpunkt, statusfähige GET-Abfragen und seller-spezifisches Laden von Produkten ergänzt.

Fachlich wurde zusätzlich klargestellt, dass das abgeschlossene Onboarding nicht automatisch zur öffentlichen Shop-Sichtbarkeit führt.

Die Shop-Freigabe bleibt Aufgabe des Admins.

Damit ist die Grundlage gelegt für Phase 4:

```text
Phase 4 – Storefront
→ öffentliche Produktübersicht
→ öffentliche Produktdetailseite
→ öffentliche Shop-Seite
→ Suche und Filter
```

Phase 4 sollte erst dann öffentliche Produkte anzeigen, wenn die Sichtbarkeitsregel vollständig berücksichtigt wird:

```text
product.status == ACTIVE
AND shop.status == ACTIVE
AND seller onboarding completed
AND shop approved by admin
```
