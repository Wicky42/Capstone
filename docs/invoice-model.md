# Invoice Model

## Überblick

Das Invoice Model beschreibt alle Rechnungs- und Abrechnungsprozesse im System.

Das System unterscheidet zwei Arten von Rechnungen:

1. `CustomerInvoice`
   → Rechnung für den Customer pro Bestellung

2. `SellerSettlement` (oder Seller Invoice)
   → monatliche Abrechnung eines Sellers gegenüber dem Plattformbetreiber (`ADMIN`)

Diese beiden Rechnungstypen sind fachlich unabhängig voneinander.

---

## Ziele des Invoice Models

Das Invoice Model soll:

* Kundenrechnungen sauber abbilden
* monatliche Seller-Abrechnungen unterstützen
* klare Trennung zwischen Kauf und Provision schaffen
* Grundlage für spätere Zahlungs- und Buchhaltungslogik sein
* Erweiterbarkeit für Steuer, PDF, Export etc. ermöglichen

---

# 1. CustomerInvoice

## Zweck

Die `CustomerInvoice` ist die Rechnung, die ein `CUSTOMER` nach Abschluss einer Bestellung erhält.

Sie basiert auf einer `FulfillmentOrder` und enthält alle bestellten Produkte über alle Shops hinweg.

---

## Beziehungen

* gehört genau zu einer `FulfillmentOrder`
* gehört genau zu einem `Customer`
* enthält mehrere `OrderItems`

---

## Felder

## Pflichtfelder

* `id`
* `fulfillmentOrderId`
* `customerId`
* `items`
* `totalAmount`
* `billingAddress`
* `createdAt`

## Optionale Felder

* `invoiceNumber`
* `pdfUrl`
* `taxAmount`
* `currency`
* `status`

---

## Feldbeschreibung

| Feld                 | Beschreibung                             | Pflicht |
| -------------------- | ---------------------------------------- | ------- |
| `id`                 | Eindeutige Rechnungs-ID                  | ja      |
| `fulfillmentOrderId` | Referenz zur Bestellung                  | ja      |
| `customerId`         | Referenz zum Customer                    | ja      |
| `items`              | Liste der bestellten Produkte (Snapshot) | ja      |
| `totalAmount`        | Gesamtbetrag der Rechnung                | ja      |
| `billingAddress`     | Rechnungsadresse (Snapshot)              | ja      |
| `createdAt`          | Erstellungszeitpunkt                     | ja      |
| `invoiceNumber`      | Lesbare Rechnungsnummer                  | nein    |
| `pdfUrl`             | Link zur PDF-Rechnung                    | nein    |
| `taxAmount`          | Steueranteil                             | nein    |
| `currency`           | Währung (z. B. EUR)                      | nein    |
| `status`             | Status der Rechnung                      | nein    |

---

## Fachliche Regeln

* Eine CustomerInvoice wird automatisch beim Check-out erzeugt
* Eine FulfillmentOrder besitzt genau eine CustomerInvoice
* Die Rechnung enthält alle Produkte aus allen Shops
* Die Rechnung basiert auf OrderItem-Snapshots
* Änderungen an Produkten beeinflussen die Rechnung nicht

---

## Beispielstruktur

```json
{
  "id": "inv-1001",
  "fulfillmentOrderId": "fo-1001",
  "customerId": "customer-123",
  "items": [
    {
      "productName": "Waldhonig 500g",
      "unitPrice": 8.99,
      "quantity": 2
    }
  ],
  "totalAmount": 17.98,
  "billingAddress": {
    "street": "Musterstraße",
    "houseNumber": "12a",
    "postalCode": "12345",
    "city": "Berlin",
    "country": "Deutschland"
  },
  "createdAt": "2026-04-22T10:00:00Z"
}
```

---

# 2. SellerSettlement (Seller-Abrechnung)

## Zweck

Die `SellerSettlement` beschreibt die monatliche Abrechnung eines `SELLER` gegenüber dem Plattformbetreiber (`ADMIN`).

Sie fasst alle Verkäufe eines Monats zusammen und berechnet die daraus resultierende Provision.

---

## Beziehungen

* gehört genau zu einem `Shop`
* gehört indirekt zu einem `Seller`
* basiert auf mehreren `SellerOrders`

---

## Felder

## Pflichtfelder

* `id`
* `shopId`
* `sellerId`
* `periodStart`
* `periodEnd`
* `totalRevenue`
* `commissionAmount`
* `createdAt`

## Optionale Felder

* `invoiceNumber`
* `status`
* `orderIds`
* `currency`
* `paidAt`

---

## Feldbeschreibung

| Feld               | Beschreibung               | Pflicht |
| ------------------ | -------------------------- | ------- |
| `id`               | Eindeutige Abrechnungs-ID  | ja      |
| `shopId`           | Referenz auf den Shop      | ja      |
| `sellerId`         | Referenz auf den Seller    | ja      |
| `periodStart`      | Startdatum der Abrechnung  | ja      |
| `periodEnd`        | Enddatum der Abrechnung    | ja      |
| `totalRevenue`     | Gesamtumsatz im Zeitraum   | ja      |
| `commissionAmount` | zu zahlende Provision      | ja      |
| `createdAt`        | Erstellungszeitpunkt       | ja      |
| `invoiceNumber`    | Rechnungsnummer            | nein    |
| `status`           | Status der Abrechnung      | nein    |
| `orderIds`         | referenzierte SellerOrders | nein    |
| `currency`         | Währung                    | nein    |
| `paidAt`           | Zahlungszeitpunkt          | nein    |

---

## Abrechnungslogik

### Monatliche Generierung

* Am Ende jedes Monats wird automatisch eine SellerSettlement erstellt
* Grundlage sind alle SellerOrders eines Shops im Zeitraum

### Inhalt

Die Abrechnung enthält:

* alle relevanten Verkäufe
* Gesamtumsatz
* berechnete Provision

---

## Status (optional)

* `OPEN`
* `PAID`
* `OVERDUE`

---

## Fachliche Regeln

* Jeder Shop erhält maximal eine Abrechnung pro Zeitraum
* Eine Abrechnung bezieht sich immer auf einen klar definierten Zeitraum
* Abrechnungen werden automatisch generiert
* Abrechnungen sind unabhängig von CustomerInvoices
* SellerOrders dienen als Grundlage für die Berechnung

---

## Abgrenzung

| Typ              | Zweck                 | Zeitpunkt |
| ---------------- | --------------------- | --------- |
| CustomerInvoice  | Rechnung für Customer | beim Kauf |
| SellerSettlement | Abrechnung für Seller | monatlich |

---

## Technische Hinweise

### Backend

* separate Collections für:

    * `customer_invoices`
    * `seller_settlements`
* Berechnung über Scheduled Jobs (z. B. monatlich)

### Frontend

* Customer sieht:

    * eigene Rechnungen
* Seller sieht:

    * monatliche Abrechnungen

---

## Erweiterungen für später

### CustomerInvoice

* PDF-Generierung
* Steuerberechnung
* Download-Funktion
* Mehrwertsteuer-Ausweisung

### SellerSettlement

* detaillierte Aufschlüsselung pro Bestellung
* variable Provisionsmodelle
* automatische Zahlung
* Erinnerungen bei offenen Beträgen

### Allgemein

* Integration mit Buchhaltungssystemen
* Export (CSV, DATEV)
* mehrere Währungen

```

---

## 🚀 Damit hast du jetzt dein komplettes Domain-Modell

Du hast jetzt sauber definiert:

- ✅ User  
- ✅ Product  
- ✅ Shop  
- ✅ Order (mit Split!)  
- ✅ Invoice / Billing  

Das ist **wirklich schon Senior-Level Architekturdenken**.

---

## 👉 Nächster sinnvoller Schritt

Jetzt gibt es zwei sehr starke Richtungen:

### Option A (sehr sinnvoll jetzt):
👉 `architecture.md`
- Wie setzt du das alles technisch um?
- Layered Architecture konkret
- Packages / Module
- API-Struktur

### Option B:
👉 direkt **Datenbank-Design (MongoDB Collections)**

---

Wenn du willst, gehen wir als nächstes in die **Architektur**, dann wird aus deiner Planung ein echtes umsetzbares System.
```
