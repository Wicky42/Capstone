# Shop Model

## Überblick

Ein Shop ist die Verkaufsoberfläche eines `SELLER` auf dem Marktplatz.
Über den Shop werden Produkte angeboten, verwaltet und für `CUSTOMER` sichtbar gemacht.

Jeder Seller besitzt genau einen Shop.
Ein Shop ist die zentrale Einheit zur Organisation von Produkten und zur Abrechnung gegenüber dem Plattformbetreiber (`ADMIN`).

---

## Ziele des Shop Models

Das Shop Model soll:

* die Struktur eines Shops definieren
* die Beziehung zwischen Seller und Produkten abbilden
* Regeln für Sichtbarkeit und Aktivierung festlegen
* die Grundlage für Shop-Darstellung im Frontend schaffen
* die Abrechnungslogik gegenüber dem Admin integrieren
* Erweiterbarkeit für zukünftige Features ermöglichen

---

## Beziehungen

Ein Shop steht in folgenden Beziehungen:

### Direkte Beziehungen

* Ein Shop gehört genau zu einem `Seller`
* Ein Shop enthält mehrere `Products`
* Ein Shop hat mehrere `Invoices` (Abrechnungen gegenüber dem Admin)

### Indirekte Beziehungen

* Ein Shop ist über Produkte mit `Customers` verbunden
* Ein Shop ist über Bestellungen mit `Orders` verknüpft
* Ein Shop ist über `OrderItems` mit Verkäufen verbunden

### Fachliche Struktur

```text
Seller -> Shop -> Products -> Orders -> OrderItems
                           -> Invoices (Seller-Abrechnung)
```

---

## Shopfelder

## Pflichtfelder

* `id`
* `name`
* `description`
* `sellerId`
* `status`
* `createdAt`
* `updatedAt`

## Optionale Felder

* `logoUrl`
* `headerImageUrl`
* `theme`
* `isVerified`
* `slug`
* `invoiceIds`

---

## Feldbeschreibung

| Feld             | Beschreibung                           | Pflicht |
| ---------------- | -------------------------------------- | ------- |
| `id`             | Eindeutige Shop-ID                     | ja      |
| `name`           | Name des Shops                         | ja      |
| `description`    | Beschreibung des Shops                 | ja      |
| `sellerId`       | Referenz auf den Seller                | ja      |
| `status`         | Status des Shops                       | ja      |
| `createdAt`      | Erstellungszeitpunkt                   | ja      |
| `updatedAt`      | Letzte Änderung                        | ja      |
| `logoUrl`        | Logo des Shops                         | nein    |
| `headerImageUrl` | Headerbild des Shops                   | nein    |
| `theme`          | Farbanpassung des Shops                | nein    |
| `isVerified`     | Verifizierungsstatus                   | nein    |
| `slug`           | URL-freundlicher Name                  | nein    |
| `invoiceIds`     | Referenzen auf monatliche Abrechnungen | nein    |

---

## Shopstatus

Ein Shop besitzt einen Status, der seine Sichtbarkeit steuert.

### Statuswerte

* `DRAFT`
* `ACTIVE`
* `INACTIVE`

---

### Bedeutung der Status

#### `DRAFT`

* Shop ist erstellt, aber noch nicht fertig konfiguriert
* Nicht sichtbar für Kunden

#### `ACTIVE`

* Shop ist öffentlich sichtbar
* Produkte können angezeigt und gekauft werden

#### `INACTIVE`

* Shop wurde deaktiviert
* Nicht sichtbar für Kunden

---

## Sichtbarkeit

Ein Shop ist nur sichtbar, wenn mehrere Bedingungen erfüllt sind.

### Ein Shop ist sichtbar, wenn:

* der Status `ACTIVE` ist
* der Seller das Onboarding abgeschlossen hat
* mindestens ein Produkt im Shop vorhanden ist

### Ein Shop ist nicht sichtbar, wenn:

* der Status `DRAFT` oder `INACTIVE` ist
* keine Produkte vorhanden sind
* der Seller nicht vollständig onboarded ist

---

## Shop-Onboarding

Das Onboarding ist ein zentraler Bestandteil des Shop Models.

### Anforderungen

Ein Seller muss im Onboarding:

* einen Shop erstellen
* grundlegende Shopdaten ausfüllen
* mindestens ein Produkt anlegen

### Abschlussbedingung

Das Onboarding gilt als abgeschlossen, wenn:

* Shopdaten vorhanden sind
* mindestens ein Produkt existiert

### Auswirkungen

* Erst danach kann der Shop sichtbar werden
* Erst danach können Produkte öffentlich angezeigt werden

---

## Shop-Verwaltung (Seller)

Ein `SELLER` kann seinen eigenen Shop verwalten.

### Erlaubte Aktionen

* Shop erstellen
* Shop bearbeiten
* Beschreibung ändern
* Logo hochladen
* Headerbild setzen
* Theme anpassen (optional)
* Produkte verwalten

### Einschränkungen

* Ein Seller darf nur seinen eigenen Shop bearbeiten
* Ein Seller kann keinen zweiten Shop erstellen

---

## Shop-Verwaltung (Admin)

Ein `ADMIN` kann Shops systemweit verwalten.

### Erlaubte Aktionen

* alle Shops einsehen
* Shops deaktivieren
* Shops prüfen
* Shops sperren oder entfernen

---

## Shop und Produkte

### Regeln

* Ein Shop kann mehrere Produkte enthalten
* Ein Shop ohne Produkte ist nicht sichtbar
* Produkte werden über den Shop organisiert
* Ein Produkt gehört immer genau zu einem Shop

---

## Navigation und Darstellung

### Funktionale Anforderungen

* Ein Customer muss über ein Produkt auf den zugehörigen Shop gelangen können
* Ein Shop muss über eine eigene Seite erreichbar sein
* Ein Shop zeigt alle zugehörigen Produkte an

### UI-Aspekte

Ein Shop kann im Frontend enthalten:

* Shopname
* Beschreibung
* Logo
* Headerbild
* Produktliste

---

## URL-Struktur (optional)

Ein Shop kann über eine URL erreichbar sein:

```text
/shop/{slug}
```

### Beispiel

```text
/shop/honigstube-mueller
```

---

## Rechnungen und Abrechnung

Ein Shop ist für die finanzielle Abrechnung gegenüber dem Plattformbetreiber verantwortlich.

---

## Abrechnungslogik

### Grundprinzip

* Seller verkaufen Produkte über den Marktplatz
* Der Admin (Plattformbetreiber) erhält eine Provision
* Die Abrechnung erfolgt gesammelt, nicht pro Bestellung

---

## Monatliche Abrechnung

### Funktionale Anforderungen

* Das System muss am Ende jedes Monats automatisch eine Abrechnung für jeden Shop erzeugen
* Diese basiert auf allen Bestellungen des jeweiligen Monats

### Inhalt einer Abrechnung

* Referenz zum Shop
* Zeitraum (Monat)
* Liste der Verkäufe oder aggregierte Werte
* Gesamtumsatz
* Provisionsbetrag
* Erstellungsdatum

---

## Fachliche Regeln (Abrechnung)

* Ein Shop kann mehrere Abrechnungen besitzen (eine pro Monat)
* Eine Abrechnung ist immer einem Zeitraum zugeordnet
* Eine Abrechnung wird automatisch generiert
* Abrechnungen sind unabhängig von Kundenrechnungen

---

## Abgrenzung der Rechnungstypen

### Kundenrechnung

* wird beim Kaufabschluss erzeugt
* geht an den Customer
* enthält alle Produkte der Bestellung

### Seller-Abrechnung

* wird monatlich erzeugt
* geht an den Seller
* enthält Verkäufe und Provisionsbetrag

---

## Abrechnungsstatus (optional)

* `OPEN`
* `PAID`
* `OVERDUE`

---

## Validierungsregeln

### Pflichtvalidierungen

* `name` darf nicht leer sein
* `description` darf nicht leer sein
* `sellerId` muss gesetzt sein
* `status` muss gesetzt sein

### Geschäftsregeln

* Ein Seller darf nur einen Shop besitzen
* Ein Shop ohne Produkte darf nicht sichtbar sein
* Ein Shop darf erst nach abgeschlossenem Onboarding aktiviert werden

---

## Technische Hinweise

### Backend

* Speicherung in `MongoDB`
* Umsetzung mit `Spring Boot`
* Referenzierung über `sellerId`

### Frontend

* Darstellung mit React
* Shop-Seite mit Produktliste

---

## Beispielstruktur

```json
{
  "id": "shop-123",
  "name": "Honigstube Müller",
  "description": "Selbstgemachter Honig aus regionaler Imkerei",
  "sellerId": "seller-456",
  "status": "ACTIVE",
  "logoUrl": "/images/logo.png",
  "headerImageUrl": "/images/header.png",
  "theme": {
    "primaryColor": "#f4c542"
  },
  "slug": "honigstube-mueller",
  "invoiceIds": ["inv-2026-03-001"],
  "createdAt": "2026-04-22T10:00:00Z",
  "updatedAt": "2026-04-22T10:00:00Z"
}
```

---

## Erweiterungen für später

### Design

* umfangreiche Theme-Anpassungen
* Templates

### Funktionen

* Featured Products
* Shop-Bewertungen
* Statistiken

### Organisation

* mehrere Shops pro Seller
* Teamzugriffe

### Abrechnung

* automatisierte Zahlung
* PDF-Export
* Erinnerungen
