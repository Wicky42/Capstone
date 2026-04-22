# Product Model

## Überblick

Ein Produkt ist ein vom `SELLER` über seinen Shop angebotener Artikel auf dem Marktplatz.  
Produkte sind die zentrale verkaufbare Einheit des Systems und können von `CUSTOMER`-Accounts gesucht, gefiltert, angesehen und bestellt werden.

Jedes Produkt gehört:

- genau zu einem `Shop`
- indirekt genau zu einem `Seller`

Ein Produkt kann in mehreren Bestellungen vorkommen und von mehreren Customers gekauft werden.

---

## Ziele des Product Models

Das Product Model soll:

- alle relevanten Produktdaten definieren
- Pflichtfelder und optionale Felder festlegen
- Beziehungen zu Shop, Seller, Warenkorb und Bestellungen beschreiben
- Regeln für Sichtbarkeit, Status und Lagerbestand definieren
- eine Grundlage für Produktverwaltung, Produktsuche und Bestelllogik schaffen

---

## Produktidentität

Jedes Produkt besitzt eine eindeutige interne ID.

### Produkt-ID
Ein Produkt soll über eine eigene ID in der Datenbank gespeichert werden.

### Fachliche Vorgabe
Die Produkt-ID kann sich zusammensetzen aus:

- dem Shop-Namen
- einer UUID oder einer zufällig generierten ID

### Beispiel
```text
honigstube-3f5a91c2