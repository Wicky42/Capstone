# Phase 3.6 – Admin Pending Verification
## Technische Zusammenfassung für Code Review

## 1. Ziel von Phase 3.6

Phase 3.6 ergänzt den Marktplatz um die fachliche und technische Freigabe von Shops durch einen Admin.

Der Schwerpunkt lag auf:

- Admin-Freigabe für abgeschlossene Seller-Onboardings
- Pending-Verification-Liste für prüfbereite Shops
- Aktivieren, Deaktivieren und Ablehnen von Shops durch Admins
- Einführung des Shop-Status `REJECTED`
- Absicherung der Admin-Endpunkte nach Rollen
- Tests für die Shop-Verifikationslogik
- Frontend-Anbindung des Admin-Bereichs
- transparenter Redirect für Admins nach dem Login

Wichtig ist dabei die fachliche Trennung:

- **Seller-Onboarding abgeschlossen** bedeutet: Der Händler hat Shopdaten und mindestens ein Produkt angelegt.
- **Shop öffentlich sichtbar** bedeutet zusätzlich: Ein Admin hat den Shop geprüft und freigegeben.

Ein Shop wird also nicht automatisch aktiv, nur weil das Onboarding abgeschlossen ist.

---

## 2. Fachliche Leitidee

Die Plattform soll verhindern, dass ein neu angelegter Shop direkt öffentlich sichtbar wird, ohne vorher geprüft worden zu sein.

Der Ablauf ist jetzt:

```text
Seller registriert sich
→ Seller erstellt Shop
→ Seller legt mindestens ein Produkt an
→ Onboarding gilt als abgeschlossen
→ Shop bleibt im Status DRAFT
→ Admin sieht Shop in Pending-Verification-Liste
→ Admin prüft Shop
→ Admin aktiviert oder lehnt Shop ab
```

Damit entsteht eine klare Verantwortlichkeit:

| Rolle | Verantwortung |
|---|---|
| Seller | Shopdaten pflegen, Produkte anlegen, Onboarding abschließen |
| Admin | Shop prüfen, aktivieren, deaktivieren oder ablehnen |
| Customer | Sieht später nur freigegebene Shops und aktive Produkte |

---

## 3. Shop-Status nach Phase 3.6

Der Shop-Lifecycle wurde um den Status `REJECTED` erweitert.

```text
DRAFT
ACTIVE
INACTIVE
REJECTED
```

### Bedeutung

| Status | Bedeutung |
|---|---|
| `DRAFT` | Shop ist erstellt, aber noch nicht durch einen Admin freigegeben |
| `ACTIVE` | Shop wurde durch einen Admin freigegeben |
| `INACTIVE` | Shop ist deaktiviert oder pausiert |
| `REJECTED` | Shop wurde durch einen Admin abgelehnt |

### Sichtbarkeit

Ein Shop ist nur öffentlich sichtbar, wenn:

```text
shop.status == ACTIVE
AND seller.onboardingCompleted == true
AND shop besitzt mindestens ein Produkt
```

Die Status `DRAFT`, `INACTIVE` und `REJECTED` sind nicht öffentlich sichtbar.

---

## 4. Was in Phase 3.6 umgesetzt wurde

## 4.1 Backend – Pending Verification

### Pending-Shops laden

Es wurde ein Admin-Endpunkt ergänzt, über den alle prüfbereiten Shops geladen werden können:

```text
GET /api/admin/shops/pending-verifications
```

Ein Shop gilt als pending, wenn:

```text
shop.status == DRAFT
AND seller.onboardingCompleted == true
```

Die Umsetzung basiert auf einer berechneten Sicht auf vorhandene Daten, statt einen zusätzlichen persistierten Pending-Status einzuführen.

### Admin-Aktionen

Der Admin kann Shops jetzt fachlich verwalten:

```text
activateShop
DeactivateShop
rejectShop
getAllPendingShops
```

Dadurch sind die zentralen Use Cases der Shop-Freigabe im Service gekapselt und nicht im Controller verteilt.

---

## 4.2 Backend – Repository-Erweiterungen

### `ShopRepository`

Es wurde `findByStatus` ergänzt.

Zweck:

```text
Alle Shops mit bestimmtem Status laden, z. B. DRAFT für Pending Verification.
```

### `UserRepository`

Es wurde `findByRole` ergänzt.

Zweck:

```text
Admin- oder rollenbezogene User-Abfragen vorbereiten.
```

---

## 4.3 Backend – DTOs

### `PendingShopVerificationResponse`

Es wurde ein eigenes Response-DTO für die Admin-Pending-Liste eingeführt.

Zweck:

```text
Admin braucht gemischte Shop- und Seller-Daten für die Prüfung.
```

Das DTO enthält fachlich relevante Informationen aus beiden Bereichen:

- Shopdaten
- Sellerdaten
- Shopstatus
- Onboarding-Informationen

Eine spätere Änderung entfernte das Feld `productNumber`, weil es für den aktuellen Flow nicht notwendig war.


---

## 4.4 Backend – Exceptions und Fehlerbehandlung

Für die Aktivierung, Deaktivierung und Ablehnung von Shops wurden neue fachliche Exceptions ergänzt.

Zweck:

- ungültige Aktivierungen sauber abfangen
- ungültige Deaktivierungen sauber abfangen
- ungültige Ablehnungen sauber abfangen
- fachliche Fehler klar von technischen Fehlern trennen

Zusätzlich wurde der `GlobalExceptionHandler` erweitert.

Später wurde der Error-Key als Konstante extrahiert, um Code-Duplizierung zu reduzieren und Sonar-Qualitätsregeln besser einzuhalten.

---

## 4.5 Backend – Security

Die Admin-Shop-Verifikation wurde rollenbasiert abgesichert.

Wichtige Regel:

```text
Nur ADMIN darf Pending-Shops sehen, Shops aktivieren, deaktivieren oder ablehnen.
```

Es wurden Security-Validierungen für Nutzer mit spezifischen Rollen ergänzt.

Dadurch wird verhindert, dass Seller oder Customer Admin-Verifikationsfunktionen ausführen können.

---

## 4.6 Backend – Onboarding-Status Bugfix

Es wurde ein wichtiger Bug behoben:

```text
seller.isOnboardingCompleted wird aktualisiert, wenn der berechnete OnboardingStatus completed ist.
```

Warum das wichtig ist:

Die Pending-Verification-Liste basiert darauf, dass der Seller fachlich als vollständig onboarded erkannt wird.

Ohne diese Aktualisierung könnte ein Shop zwar alle Onboarding-Bedingungen erfüllen, aber trotzdem nicht korrekt in der Admin-Pending-Liste erscheinen.

---

## 4.7 Backend – Tests und Qualität

Es wurden Tests für `AdminShopVerification` ergänzt.

Abgedeckt werden insbesondere:

- Pending-Shops laden
- Shop aktivieren
- Shop deaktivieren
- Shop ablehnen
- Rollen- und Security-Regeln
- ungültige Statusübergänge
- fachliche Validierungsfehler


---

# 5. Frontend – Admin Pending Verification

## 5.1 Frontend-Typen

Es wurden neue Typen ergänzt:

```text
ShopStatus
PendingShopVerification
```

`ShopStatus` enthält jetzt auch:

```text
REJECTED
```

Damit kann das Frontend alle Shop-Zustände typisiert darstellen.

---

## 5.2 Admin Service

Es wurde ein `adminService` ergänzt.

Verantwortung:

```text
activateShop
deactivateShop
rejectShop
Pending-Shops laden
```

Dadurch liegen API-Aufrufe nicht direkt in Pages oder UI-Komponenten.

Das folgt der bereits etablierten Frontend-Struktur, in der Services die Kommunikation mit dem Backend kapseln.

---

## 5.3 Admin Routes

Es wurden Admin-Routen ergänzt und Pfade angepasst.

Wichtige Änderung:

```text
/admin
```

führt in den Admin-Bereich für Pending Verifications.

Zusätzlich wurde der Login-Redirect für Admins angepasst:

```text
vorher: /
nachher: /admin
```

Damit landen Admins nach dem Login nicht mehr auf der Customer-Startseite, sondern direkt im passenden Arbeitsbereich.

---

## 5.4 PendingVerificationPage und PendingVerificationTable

Es wurden folgende UI-Bausteine ergänzt:

```text
PendingVerificationPage
PendingVerificationTable
```

Zweck:

- Admin sieht alle Shops, die auf Prüfung warten
- Admin kann Shopdaten und Sellerdaten einsehen
- Admin kann Shops aktivieren
- Admin kann Shops ablehnen

Die Tabelle trennt Darstellung und Datenlogik sauber von der Page.

---

# 6. Aktueller Admin-Flow

## 6.1 Shop aktivieren

```text
Admin öffnet /admin
→ PendingVerificationPage lädt prüfbereite Shops
→ Admin klickt Aktivieren
→ Backend setzt Shop auf ACTIVE
→ Shop verschwindet aus Pending-Liste
```

## 6.2 Shop ablehnen

```text
Admin öffnet /admin
→ PendingVerificationPage lädt prüfbereite Shops
→ Admin klickt Ablehnen
→ Backend setzt Shop auf REJECTED
→ Shop verschwindet aus Pending-Liste
```

## 6.3 Shop deaktivieren

```text
Admin kann einen Shop deaktivieren
→ Backend setzt Shop auf INACTIVE
→ Shop ist nicht öffentlich sichtbar
```

---

# 7. Wichtige technische Entscheidungen

## 7.1 Kein eigener Pending-Status

Es wurde kein zusätzlicher Status `PENDING_VERIFICATION` eingeführt.

Pending wird berechnet aus:

```text
Shop ist DRAFT
Seller-Onboarding ist abgeschlossen
```

Das hält das Modell einfacher.

## 7.2 `REJECTED` als eigener Status

Ablehnung wird nicht über `INACTIVE` abgebildet.

Das ist fachlich sauberer, weil:

```text
INACTIVE = deaktiviert / pausiert
REJECTED = durch Admin abgelehnt
```

So kann das Frontend dem Seller später eine deutlichere Rückmeldung geben.

## 7.3 Admin-Freigabe ist getrennt vom Seller-Onboarding

Das Seller-Onboarding entscheidet nur, ob ein Händler alle erforderlichen Angaben gemacht hat.

Die Admin-Freigabe entscheidet, ob der Shop öffentlich sichtbar werden darf.

Diese Trennung ist wichtig für Plattformkontrolle und spätere Admin-Funktionen.

---

# 8. Offene Punkte / nächste Schritte

## 8.1 SellerShopEditPage weiter ausbauen

Die `SellerShopEditPage` existiert bereits aus Phase 3.5, ist aber fachlich noch weiter auszubauen.

Der Seller soll dort ändern können:

- Händlername / Business Name
- Adresse
- Shopname
- Shopbeschreibung
- ggf. Logo und Headerbild

Wichtig:

```text
Seller darf seinen Shop nicht selbst aktivieren.
```

Erlaubt ist nur:

```text
ACTIVE → INACTIVE
```

Nicht erlaubt:

```text
DRAFT → ACTIVE
INACTIVE → ACTIVE
REJECTED → ACTIVE
```

## 8.2 Seller Dashboard um Freigabestatus erweitern

Das Seller Dashboard sollte klar anzeigen:

| Status | Anzeige |
|---|---|
| `DRAFT` | Shop wartet auf Admin-Freigabe |
| `ACTIVE` | Shop ist freigegeben |
| `INACTIVE` | Shop ist deaktiviert |
| `REJECTED` | Shop wurde abgelehnt |

Besonders bei `REJECTED` sollte ein klarer nächster Schritt angezeigt werden:

```text
Bitte prüfe und aktualisiere deine Shopdaten.
```

## 8.3 Optional: Ablehnungsgrund

Für spätere Erweiterungen wäre ein Ablehnungsgrund sinnvoll.

Beispiel:

```text
Shop wurde abgelehnt, weil Adresse oder Produktangaben unvollständig sind.
```

Dafür wären später nötig:

- Feld für Ablehnungsgrund
- Admin-UI mit Texteingabe
- Seller-UI zur Anzeige des Grundes

Für den aktuellen Stand ist das noch nicht zwingend erforderlich.

## 8.4 Admin-Bereich später erweitern

Phase 3.6 ist bewusst nur ein kleiner Admin-Use-Case.

Das vollständige Admin-Panel bleibt weiterhin späteren Phasen vorbehalten, z. B.:

- User Management
- Produktverwaltung
- Bestellübersicht
- Rechnungen
- Rollenänderungen

---

# 9. Definition of Done – Phase 3.6

Phase 3.6 ist abgeschlossen, wenn:

- Admins nach dem Login auf `/admin` landen
- Admins eine Pending-Verification-Liste sehen
- Pending-Shops aus `DRAFT` + abgeschlossenem Seller-Onboarding berechnet werden
- Admins Shops aktivieren können
- Admins Shops ablehnen können
- Admins Shops deaktivieren können (in der pending List noch nicht möglich -> späterer Zeitpunkt, beim Anzeigen aller Shops möglich)
- `REJECTED` als eigener Shopstatus existiert
- Seller-Onboarding korrekt auf completed gesetzt wird
- Admin-Endpunkte rollenbasiert abgesichert sind
- Tests für AdminShopVerification vorhanden sind
- Frontend-Typen und Admin-Service vorhanden sind
- PendingVerificationPage und PendingVerificationTable vorhanden sind

---

# 10. Fazit

Phase 3.6 schließt eine wichtige fachliche Lücke zwischen Seller-Onboarding und öffentlicher Storefront.

Vor dieser Phase konnte ein Seller fachlich sein Onboarding abschließen, aber die Plattform hatte noch keinen sauberen Prüf- und Freigabeprozess.

Jetzt ist klar:

```text
Seller liefert Daten
Admin prüft
Admin aktiviert
Storefront zeigt später nur freigegebene Shops
```

Damit ist die Grundlage für Phase 4 stabiler, weil die öffentliche Produkt- und Shopanzeige auf einer eindeutigen Freigabeentscheidung aufbauen kann.
