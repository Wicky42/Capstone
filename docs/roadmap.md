# Roadmap

## Ziel

Strukturierte Umsetzung des MVP in klaren Phasen.

---

## Phase 1 – Auth & User

* GitHub OAuth einrichten
* `/api/auth/me`
* Rollenmodell
* RegisterPage (Rollenwahl)
* User persistieren
* Initialer Admin

---

## Phase 2 – Seller Onboarding & Shop

* Seller-Onboarding-Seite
* Shop erstellen
* Shop sichtbar/nicht sichtbar Logik
* `/api/seller/shops`

---

## Phase 3 – Produkte

* Produkt erstellen
* Produkt bearbeiten/löschen
* Bildupload
* Produktstatus
* `/api/products`
* Suche + Filter

---

## Phase 4 – Storefront

* Homepage (Produkte)
* Filter (Kategorie)
* Suche
* Produktdetailseite
* Shop-Seite

---

## Phase 5 – Cart ✅

* Warenkorb anlegen (client-seitig, `localStorage`)
* Produkte hinzufügen (Snapshot-Prinzip)
* Menge ändern
* Warenkorb anzeigen (`CartPage`)
* Live-Verfügbarkeitsprüfung
* Header-Badge
* „Zur Kasse"-Button als Platzhalter (Phase 6)

---

## Phase 6 – Checkout & Orders (Core)

> Detailspezifikation: [`docs/todo/phase-6-checkout-flows.md`](todo/phase-6-checkout-flows.md)
> Tickets: [`docs/todo/phase-6-tickets.md`](../docs/todo/phase-6-tickets.md)

### Übergang Phase 5 → Phase 6

* „Zur Kasse"-Button aktivieren
* Auth-Guard: nur `CUSTOMER` darf Checkout starten
* Redirect-Logik nach Login (via `sessionStorage`)

### Backend

* `TICKET-6.1` – Models & Repositories: `FulfillmentOrder`, `SellerOrder`, `OrderItem`, `CustomerInvoice`
* `TICKET-6.2` – `CheckoutService`: Validierung, FulfillmentOrder, SellerOrder-Split, Invoice
* `TICKET-6.3` – `CheckoutController`: `POST /api/orders/checkout`
* `TICKET-6.4` – `CustomerOrderController`: Bestellhistorie & Rechnung
* `TICKET-6.5` – `SellerOrderController`: SellerOrders lesen + Status-Update
* `TICKET-6.6` – `AdminOrderController`: Gesamtübersicht + Status-Update
* `TICKET-6.7` – `WarehouseProperties`: Zentrallager-Adresse aus `application.properties`

### Frontend

* `TICKET-6.8` – Auth-Guard auf Checkout (Redirect zu Login/Register)
* `TICKET-6.9` – `CheckoutPage` Schritt 1: Adressformular
* `TICKET-6.10` – `CheckoutPage` Schritt 2: Bestellübersicht + API-Call + Cart leeren
* `TICKET-6.11` – `OrderConfirmationPage`
* `TICKET-6.12` – `AccountOrdersPage` + `AccountOrderDetailPage`
* `TICKET-6.13` – `SellerOrdersPage` + Status-Update
* `TICKET-6.14` – `AdminOrdersPage` + Status-Update

---

## Phase 7 – Seller Orders

* SellerOrder Ansicht (Detail)
* Statusverlauf vollständig abbilden
* Warehouse-Logik (Zentrallager-Adresse)
* Benachrichtigung bei neuer Bestellung (In-App)

---

## Phase 8 – Admin

* User Management
* Rollen ändern
* Produktverwaltung
* Order-Übersicht (FulfillmentOrders + SellerOrders)
* FulfillmentOrder-Status manuell steuern

---

## Phase 9 – Invoices & Settlement

* CustomerInvoice anzeigen
* SellerSettlement monatlich
* Scheduled Job

---

## Phase 10 – Polish

* Fehlerhandling
* Loading States
* UX Verbesserungen
* Security Checks

---

## MVP Abschluss

Nach Phase 10 ist das System:

* voll funktionsfähig
* stabil
* bereit für erste Nutzer

---

## Post-MVP

* PayPal Integration
* mehrere Zentrallager
* Bewertungen
* Produktvarianten
* SEO
* Mobile Optimierung
