# Order Model

## Überblick

Das Bestellmodell bildet den vollständigen Kaufprozess des Marktplatzes ab.

Da ein `CUSTOMER` Produkte aus mehreren Shops in einem gemeinsamen Warenkorb bestellen kann, wird eine Bestellung im System in zwei Ebenen unterteilt:

* `FulfillmentOrder`
  Die Gesamtbestellung aus Sicht des Customers

* `SellerOrder`
  Die seller-spezifische Teilbestellung aus Sicht eines einzelnen Sellers

Der Customer sieht ausschließlich die `FulfillmentOrder`.
Seller sehen ausschließlich die für sie relevanten `SellerOrder`-Einträge.
Admins können sowohl die Gesamtbestellung als auch alle Teilbestellungen einsehen.

---

## Ziele des Order Models

Das Order Model soll:

* den Multi-Seller-Bestellprozess abbilden
* Gesamtbestellung und Teilbestellungen trennen
* den zentralen Fulfillment-Prozess unterstützen
* Rechnungen und Zahlungsstatus integrieren
* Statusverläufe für Customer- und Seller-Sicht definieren
* die Grundlage für spätere Erweiterungen wie Stornierungen und Teilablehnungen schaffen

---

## Bestellstruktur

## Entitäten

Das System verwendet zwei Hauptentitäten:

### 1. FulfillmentOrder

Die `FulfillmentOrder` ist die Gesamtbestellung des Customers.

Sie enthält:

* alle bestellten Produkte
* den Gesamtpreis
* die Liefer- und Rechnungsadresse
* die Kundenrechnung
* den übergeordneten Bestellstatus

### 2. SellerOrder

Die `SellerOrder` ist die seller-spezifische Teilbestellung.

Sie enthält:

* nur die Produkte eines bestimmten Sellers
* den seller-spezifischen Status
* die Lieferadresse des Zentrallagers
* die Referenz zur übergeordneten FulfillmentOrder

---

## Beziehungen

### FulfillmentOrder

* gehört genau zu einem `Customer`
* hat mindestens eine `SellerOrder`
* hat mehrere `OrderItems`
* hat genau eine Kundenrechnung (`Invoice`)
* kann mehrere beteiligte Shops enthalten

### SellerOrder

* gehört genau zu einer `FulfillmentOrder`
* gehört genau zu einem `Seller`
* gehört genau zu einem `Shop`
* hat mehrere `OrderItems`

### OrderItem

* gehört genau zu einer Bestellung
* speichert Produktdaten als Snapshot zum Bestellzeitpunkt

---

## Fachliche Struktur

```text
Customer
  -> FulfillmentOrder
      -> SellerOrder 1
      -> SellerOrder 2
      -> SellerOrder n
```

---

## FulfillmentOrder

## Zweck

Die `FulfillmentOrder` ist die Bestellung, die der Customer im Frontend sieht und die beim Checkout entsteht.

Sie bündelt alle Produkte des Warenkorbs, auch wenn diese von mehreren Sellern stammen.

---

## Felder der FulfillmentOrder

## Pflichtfelder

* `id`
* `customerId`
* `items`
* `sellerOrderIds`
* `shopIds`
* `totalPrice`
* `shippingAddress`
* `billingAddress`
* `invoiceId`
* `isPaid`
* `status`
* `createdAt`

## Optionale Felder

* `updatedAt`
* `readyForFinalShipment`
* `completedAt`

---

## Feldbeschreibung

| Feld                    | Beschreibung                        | Pflicht |
| ----------------------- | ----------------------------------- | ------- |
| `id`                    | Eindeutige ID der Gesamtbestellung  | ja      |
| `customerId`            | Referenz auf den Customer           | ja      |
| `items`                 | Alle bestellten OrderItems          | ja      |
| `sellerOrderIds`        | Referenzen auf die Teilbestellungen | ja      |
| `shopIds`               | Liste beteiligter Shops             | ja      |
| `totalPrice`            | Gesamtpreis der Bestellung          | ja      |
| `shippingAddress`       | Lieferadresse als Snapshot          | ja      |
| `billingAddress`        | Rechnungsadresse als Snapshot       | ja      |
| `invoiceId`             | Referenz auf die Kundenrechnung     | ja      |
| `isPaid`                | Zahlungsstatus                      | ja      |
| `status`                | Gesamtstatus der Bestellung         | ja      |
| `createdAt`             | Erstellungszeitpunkt                | ja      |
| `updatedAt`             | Letzte Änderung                     | nein    |
| `readyForFinalShipment` | Kennzeichen für Versandbereitschaft | nein    |
| `completedAt`           | Abschlusszeitpunkt                  | nein    |

---

## Status der FulfillmentOrder

### Erlaubte Statuswerte

* `CREATED`
* `PROCESSING`
* `READY_FOR_FINAL_SHIPMENT`
* `COMPLETED`
* `CANCELLED`

---

### Bedeutung der Status

#### `CREATED`

* Bestellung wurde erfolgreich aufgegeben
* Rechnung wurde erzeugt
* Teilbestellungen wurden erstellt
* Bestellung gilt im MVP bereits als bezahlt

#### `PROCESSING`

* Teilbestellungen werden von den Sellern bearbeitet
* Produkte werden vorbereitet oder an das Zentrallager gesendet

#### `READY_FOR_FINAL_SHIPMENT`

* Alle SellerOrders wurden erfolgreich an das Zentrallager geliefert
* Die Gesamtbestellung kann final verpackt und an den Customer versendet werden

#### `COMPLETED`

* Die Gesamtbestellung wurde vollständig abgeschlossen und versendet

#### `CANCELLED`

* Bestellung wurde storniert oder ungültig gemacht
* Im MVP derzeit nicht aktiv genutzt, aber bereits fachlich vorgesehen

---

## Fachliche Regeln der FulfillmentOrder

* Jede FulfillmentOrder muss mindestens eine SellerOrder besitzen
* Eine FulfillmentOrder kann mehrere SellerOrders besitzen
* Der Customer darf ausschließlich die FulfillmentOrder sehen
* Der Customer darf die internen SellerOrders nicht sehen
* Die Liefer- und Rechnungsadresse werden als Snapshot gespeichert
* Die FulfillmentOrder gilt im MVP sofort als bezahlt (`isPaid = true`)
* Eine FulfillmentOrder darf erst auf `READY_FOR_FINAL_SHIPMENT` wechseln, wenn alle SellerOrders abgeschlossen bzw. an das Zentrallager geliefert wurden

---

## SellerOrder

## Zweck

Die `SellerOrder` ist die seller-spezifische Teilbestellung, die intern aus einer FulfillmentOrder erzeugt wird.

Sie dient dazu, jedem Seller nur die Produkte anzuzeigen, die für seinen Shop bestellt wurden.

---

## Felder der SellerOrder

## Pflichtfelder

* `id`
* `fulfillmentOrderId`
* `sellerId`
* `shopId`
* `items`
* `warehouseAddress`
* `status`
* `createdAt`

## Optionale Felder

* `updatedAt`
* `completedAt`

---

## Feldbeschreibung

| Feld                 | Beschreibung                      | Pflicht |
| -------------------- | --------------------------------- | ------- |
| `id`                 | Eindeutige ID der Teilbestellung  | ja      |
| `fulfillmentOrderId` | Referenz auf die Gesamtbestellung | ja      |
| `sellerId`           | Referenz auf den Seller           | ja      |
| `shopId`             | Referenz auf den Shop             | ja      |
| `items`              | OrderItems des Sellers            | ja      |
| `warehouseAddress`   | Lieferadresse des Zentrallagers   | ja      |
| `status`             | Status der Teilbestellung         | ja      |
| `createdAt`          | Erstellungszeitpunkt              | ja      |
| `updatedAt`          | Letzte Änderung                   | nein    |
| `completedAt`        | Abschlusszeitpunkt                | nein    |

---

## Status der SellerOrder

### Erlaubte Statuswerte

* `CREATED`
* `CONFIRMED`
* `IN_PREPARATION`
* `SHIPPED_TO_WAREHOUSE`
* `COMPLETED`
* `CANCELLED`

---

### Bedeutung der Status

#### `CREATED`

* SellerOrder wurde aus der FulfillmentOrder erzeugt

#### `CONFIRMED`

* Seller hat die Bestellung gesehen bzw. bestätigt

#### `IN_PREPARATION`

* Seller bereitet die Produkte für den Versand an das Zentrallager vor

#### `SHIPPED_TO_WAREHOUSE`

* Produkte wurden vom Seller an das Zentrallager versendet

#### `COMPLETED`

* Seller-Anteil der Bestellung ist abgeschlossen

#### `CANCELLED`

* Teilbestellung wurde storniert oder aufgehoben
* Im MVP noch nicht aktiv genutzt

---

## Fachliche Regeln der SellerOrder

* Jede SellerOrder gehört genau zu einem Seller
* Jede SellerOrder gehört genau zu einem Shop
* Jede SellerOrder enthält nur Produkte eines Sellers
* Seller sehen ausschließlich ihre eigenen SellerOrders
* Seller sehen nicht die gesamte FulfillmentOrder
* Eine SellerOrder enthält die Lieferadresse des Zentrallagers
* Erst wenn alle SellerOrders einer FulfillmentOrder mindestens `SHIPPED_TO_WAREHOUSE` oder `COMPLETED` erreicht haben, kann die FulfillmentOrder in den finalen Versand übergehen

---

## OrderItem

## Zweck

Ein `OrderItem` repräsentiert eine bestellte Produktposition innerhalb einer FulfillmentOrder oder SellerOrder.

Damit spätere Änderungen am Produkt keine historischen Bestellungen verfälschen, werden bestellrelevante Produktdaten als Snapshot gespeichert.

---

## Snapshot-Prinzip

Ein OrderItem speichert nicht nur die Produkt-ID, sondern auch relevante Daten zum Zeitpunkt des Kaufs.

Dadurch bleiben Bestellungen stabil, selbst wenn der Seller später:

* den Produktnamen ändert
* den Preis ändert
* Bilder austauscht
* die Beschreibung anpasst

---

## Felder des OrderItem

## Pflichtfelder

* `productId`
* `productName`
* `unitPrice`
* `quantity`
* `titleImage`
* `shopId`
* `sellerId`

## Optionale Felder

* `productDescription`
* `category`
* `snapshotCreatedAt`

---

## Feldbeschreibung

| Feld                 | Beschreibung                           | Pflicht |
| -------------------- | -------------------------------------- | ------- |
| `productId`          | Referenz auf das ursprüngliche Produkt | ja      |
| `productName`        | Produktname zum Kaufzeitpunkt          | ja      |
| `unitPrice`          | Einzelpreis zum Kaufzeitpunkt          | ja      |
| `quantity`           | Bestellte Menge                        | ja      |
| `titleImage`         | Titelbild zum Kaufzeitpunkt            | ja      |
| `shopId`             | Shop des Produkts                      | ja      |
| `sellerId`           | Seller des Produkts                    | ja      |
| `productDescription` | Beschreibung zum Kaufzeitpunkt         | nein    |
| `category`           | Kategorie zum Kaufzeitpunkt            | nein    |
| `snapshotCreatedAt`  | Zeitpunkt der Snapshot-Erstellung      | nein    |

---

## Fachliche Regeln des OrderItem

* Ein OrderItem muss immer eine Menge besitzen
* Die Menge muss größer als 0 sein
* Der Preis wird zum Kaufzeitpunkt fixiert
* Das Titelbild wird zum Kaufzeitpunkt fixiert
* Ein OrderItem darf nicht nachträglich durch Produktänderungen beeinflusst werden

---

## Zahlungslogik

## MVP-Regel

Im MVP gilt eine Bestellung sofort als bezahlt, sobald der Customer die Bestellung aufgibt.

### Fachliche Regel

* `isPaid = true`, sobald die FulfillmentOrder erstellt wurde

### Konsequenz

* Nach Erstellung der FulfillmentOrder können alle weiteren Fulfillment-Prozesse starten
* Eine separate Zahlungsprüfung findet im MVP nicht statt

### Hinweis

Diese Regel dient nur der Vereinfachung in Version 1 und soll später durch echte Zahlungslogik ersetzt werden.

---

## Rechnungen

## Kundenrechnung

* Wird automatisch beim Kaufabschluss erzeugt
* Gehört genau zu einer FulfillmentOrder
* Wird dem Customer angezeigt
* Enthält alle Produkte der Gesamtbestellung

## Seller-Abrechnung

* Gehört nicht direkt zur SellerOrder
* SellerOrders fließen in die monatliche Seller-Abrechnung ein
* Die monatliche Abrechnung wird auf Shop-Ebene erzeugt

### Abgrenzung

* FulfillmentOrder -> Kundenrechnung
* SellerOrder -> Grundlage für monatliche Seller-Abrechnung

---

## Versandlogik / Zentrallager

Das System nutzt ein zentrales Fulfillment.

### Fachlicher Ablauf

1. Customer gibt eine FulfillmentOrder auf
2. Das System erzeugt daraus SellerOrders
3. Jeder Seller sieht nur seine eigene SellerOrder
4. Seller versendet seine Produkte an das Zentrallager
5. Sobald alle SellerOrders im Zentrallager angekommen sind, wird die FulfillmentOrder für den Endversand vorbereitet
6. Der Systembetreiber versendet die Gesamtbestellung an den Customer

### Regeln

* Es gibt im MVP genau ein Zentrallager
* Die Lieferadresse des Zentrallagers wird in jeder SellerOrder gespeichert
* Seller dürfen diese Adresse sehen

### Erweiterung für später

* Unterstützung mehrerer Zentrallager
* Auswahl logistischer Optionen nach Entfernung oder Kosten

---

## Sichtbarkeit pro Rolle

## Customer sieht

* die FulfillmentOrder
* alle bestellten Produkte
* Gesamtpreis
* Lieferadresse
* Rechnungsadresse
* Rechnung
* Status der Gesamtbestellung
* beteiligte Shops

## Seller sieht

* nur die eigene SellerOrder
* nur die eigenen bestellten Produkte
* Mengen
* seller-spezifischen Status
* Lieferadresse des Zentrallagers

## Admin sieht

* FulfillmentOrders
* SellerOrders
* beteiligte Shops
* beteiligte Seller
* Bestell- und Statusverläufe

---

## Validierungsregeln

### FulfillmentOrder

* `customerId` muss gesetzt sein
* `items` darf nicht leer sein
* `sellerOrderIds` darf nicht leer sein
* `totalPrice` muss größer als 0 sein
* `shippingAddress` muss gesetzt sein
* `billingAddress` muss gesetzt sein
* `invoiceId` muss gesetzt sein
* `status` muss gesetzt sein

### SellerOrder

* `fulfillmentOrderId` muss gesetzt sein
* `sellerId` muss gesetzt sein
* `shopId` muss gesetzt sein
* `items` darf nicht leer sein
* `warehouseAddress` muss gesetzt sein
* `status` muss gesetzt sein

### OrderItem

* `productId` muss gesetzt sein
* `productName` darf nicht leer sein
* `unitPrice` muss größer als 0 sein
* `quantity` muss größer als 0 sein
* `shopId` muss gesetzt sein
* `sellerId` muss gesetzt sein

---

## MVP-Einschränkungen

Im MVP gelten folgende Vereinfachungen:

* keine Stornierungen durch Customer
* keine Teilablehnungen durch Seller
* keine Rückabwicklung von Bestellungen
* keine echte Zahlungsprüfung
* genau ein Zentrallager

Die Status und Felder für spätere Erweiterungen sind dennoch bereits vorgesehen.

---

## Technische Hinweise für die Umsetzung

### Backend

* Umsetzung mit `Spring Boot`
* Speicherung in `MongoDB`
* FulfillmentOrder und SellerOrder als eigene Collections oder klar getrennte Aggregate modellieren
* OrderItem als eingebettete Struktur mit Snapshot-Daten verwenden

### Frontend

* Customer erhält eine Bestellübersicht über FulfillmentOrders
* Seller erhält eine Bestellübersicht über SellerOrders
* Admin erhält Gesamtübersicht über beide Ebenen

---

## Beispielstruktur: FulfillmentOrder

```json
{
  "id": "fo-1001",
  "customerId": "customer-123",
  "items": [
    {
      "productId": "honigstube-3f5a91c2",
      "productName": "Waldhonig 500g",
      "unitPrice": 8.99,
      "quantity": 2,
      "titleImage": "/images/honey-main.jpg",
      "shopId": "shop-123",
      "sellerId": "seller-456"
    },
    {
      "productId": "jamshop-a81d22b1",
      "productName": "Erdbeermarmelade",
      "unitPrice": 5.49,
      "quantity": 1,
      "titleImage": "/images/jam-main.jpg",
      "shopId": "shop-789",
      "sellerId": "seller-999"
    }
  ],
  "sellerOrderIds": ["so-2001", "so-2002"],
  "shopIds": ["shop-123", "shop-789"],
  "totalPrice": 23.47,
  "shippingAddress": {
    "street": "Musterstraße",
    "houseNumber": "12a",
    "postalCode": "12345",
    "city": "Berlin",
    "country": "Deutschland"
  },
  "billingAddress": {
    "street": "Musterstraße",
    "houseNumber": "12a",
    "postalCode": "12345",
    "city": "Berlin",
    "country": "Deutschland"
  },
  "invoiceId": "inv-3001",
  "isPaid": true,
  "status": "PROCESSING",
  "createdAt": "2026-04-22T10:00:00Z"
}
```

---

## Beispielstruktur: SellerOrder

```json
{
  "id": "so-2001",
  "fulfillmentOrderId": "fo-1001",
  "sellerId": "seller-456",
  "shopId": "shop-123",
  "items": [
    {
      "productId": "honigstube-3f5a91c2",
      "productName": "Waldhonig 500g",
      "unitPrice": 8.99,
      "quantity": 2,
      "titleImage": "/images/honey-main.jpg",
      "shopId": "shop-123",
      "sellerId": "seller-456"
    }
  ],
  "warehouseAddress": {
    "street": "Lagerstraße",
    "houseNumber": "1",
    "postalCode": "10115",
    "city": "Berlin",
    "country": "Deutschland"
  },
  "status": "IN_PREPARATION",
  "createdAt": "2026-04-22T10:00:00Z"
}
```

---

## Erweiterungen für später

* Stornierungen durch Customer
* Teilablehnungen durch Seller
* Teilrückerstattungen
* mehrere Zentrallager
* echte Zahlungsabwicklung
* Versandtracking
* Lieferstatus pro SellerOrder
* Teillieferungen
* Benachrichtigungen und Eskalationen
