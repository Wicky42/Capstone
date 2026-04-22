# User Model

## Überblick

Das System kennt drei Benutzertypen:

- `ADMIN`
- `SELLER`
- `CUSTOMER`

Jeder User besitzt **genau eine Rolle**.  
Ein User kann zunächst **nicht mehrere Rollen gleichzeitig** haben.

Die Authentifizierung erfolgt ausschließlich über **OAuth mit GitHub**.  
Vor der Authentifizierung entscheidet der Nutzer, ob er sich als `SELLER` oder als `CUSTOMER` registrieren möchte.

## Ziele des User Models

Das User Model soll:

- die verschiedenen Rollen im System eindeutig abbilden
- gemeinsame und rollenspezifische Felder definieren
- die Grundlage für Authentifizierung und Autorisierung bilden
- Beziehungen zu Shops, Bestellungen und Warenkörben beschreiben
- Onboarding-Regeln für Seller festhalten

---

## Rollen

### ADMIN
Ein Admin verwaltet die Plattform und hat Zugriff auf alle relevanten Systemdaten.

Rechte:
- Einsicht in alle Käufer
- Einsicht in alle Händler
- Einsicht in alle Bestellungen
- Einsicht in Rechnungen
- Verwaltungsfunktionen im Admin-Bereich
- andere Nutzer zu Admins machen

Regeln:
- Initial existiert genau ein Admin im System
- Weitere Admins können nur durch einen bestehenden Admin angelegt oder freigeschaltet werden
- Ein normaler User kann sich nicht selbst als Admin registrieren

### SELLER
Ein Seller ist ein Händler, der selbstgemachte Lebensmittel über den Marktplatz verkauft.

Rechte:
- eigenen Shop verwalten
- Produkte anlegen und bearbeiten
- Bestellungen für den eigenen Shop einsehen
- Shop-Daten anpassen

Besonderheit:
- Ein Seller besitzt **genau einen Shop**
- Ein Seller muss ein Onboarding absolvieren
- Ein Shop ist erst sichtbar, wenn das Onboarding abgeschlossen wurde
- Es können sich nur Händler aus Deutschland registrieren

### CUSTOMER
Ein Customer ist ein Käufer auf dem Marktplatz.

Rechte:
- Produkte durchsuchen
- Produkte in den Warenkorb legen
- Bestellungen aufgeben
- eigene Bestellungen einsehen

---

## Authentifizierung

### Auth Provider
Die Authentifizierung erfolgt über:

- `GitHub OAuth`

### Registrierungslogik
Vor dem OAuth-Login wählt der Nutzer aus, ob er sich registrieren möchte als:

- `SELLER`
- `CUSTOMER`

Diese Entscheidung wird vor dem Login-Prozess erfasst und nach erfolgreicher Authentifizierung dem neuen User-Profil zugeordnet.

### Regeln
- Jeder User besitzt genau einen eindeutigen OAuth-Account
- Ein OAuth-Account ist genau einem internen User zugeordnet
- Pro User gibt es zunächst keine Verknüpfung mit mehreren OAuth-Accounts
- Es existiert kein klassischer Login mit E-Mail und Passwort

---

## Gemeinsame User-Felder

Jeder User besitzt die folgenden Basisdaten:

- `id`
- `role`
- `name`
- `email`
- `oauthProvider`
- `oauthProviderUserId`
- `createdAt`
- `updatedAt`

### Feldbeschreibung

| Feld | Beschreibung |
|------|--------------|
| `id` | Eindeutige interne ID des Users |
| `role` | Rolle des Users (`ADMIN`, `SELLER`, `CUSTOMER`) |
| `name` | Anzeigename des Users |
| `email` | E-Mail-Adresse des Users |
| `oauthProvider` | Auth-Provider, initial `GITHUB` |
| `oauthProviderUserId` | Eindeutige ID des Users beim OAuth-Provider |
| `createdAt` | Erstellungsdatum |
| `updatedAt` | Letztes Änderungsdatum |

---

## Seller-spezifische Felder

Zusätzlich zu den gemeinsamen Basisdaten besitzt ein Seller folgende Informationen:

- `businessName`
- `description`
- `logoUrl`
- `address`
- `billingAddress`
- `taxId`
- `shopId`
- `onboardingCompleted`

### Beschreibung

| Feld | Beschreibung |
|------|--------------|
| `businessName` | Firmenname oder Name des Gewerbes |
| `description` | Beschreibung des Sellers / Geschäfts |
| `logoUrl` | Logo des Sellers |
| `address` | Geschäftsadresse in Deutschland |
| `billingAddress` | Rechnungsadresse |
| `taxId` | Steuer-ID des Händlers |
| `shopId` | Referenz auf den Shop des Sellers |
| `onboardingCompleted` | Gibt an, ob das Seller-Onboarding abgeschlossen wurde |

### Fachliche Regeln
- Ein Seller besitzt **genau einen Shop**
- Ein Seller muss im Onboarding:
    - seinen Shop aufsetzen
    - mindestens ein Produkt einstellen
- Solange `onboardingCompleted = false`, ist der Shop **nicht öffentlich sichtbar**
- Erst nach abgeschlossenem Onboarding kann der Seller aktiv auf dem Marktplatz verkaufen
- Seller dürfen nur mit deutscher Adresse registriert werden

---

## Customer-spezifische Felder

Zusätzlich zu den gemeinsamen Basisdaten besitzt ein Customer folgende Informationen:

- `shippingAddress`
- `billingAddress`
- `cart`
- `orderIds`

### Beschreibung

| Feld | Beschreibung |
|------|--------------|
| `shippingAddress` | Lieferadresse des Customers |
| `billingAddress` | Rechnungsadresse des Customers |
| `cart` | Aktueller Warenkorb des Customers |
| `orderIds` | Liste der Bestellungen des Customers |

### Fachliche Regeln
- Ein Customer besitzt genau einen aktiven Warenkorb
- Ein Customer kann mehrere Bestellungen besitzen
- Die Zahlungsmethode wird nicht dauerhaft gespeichert
- Die Zahlungsmethode wird bei jeder Bestellung neu ausgewählt
- Initial wird nur `VORKASSE` unterstützt
- Später sollen externe Zahlungsanbieter wie `PayPal` angebunden werden

---

## Admin-spezifische Felder

Für Admins sind zunächst keine zusätzlichen Profildaten vorgesehen.

Ein Admin wird aktuell ausschließlich über die Rolle definiert:

- `role = ADMIN`

---

## Rollenwechsel

Rollenwechsel sind fachlich stark eingeschränkt.

### Regeln
- Ein bestehender `CUSTOMER` kann nicht regulär selbst zu einem `SELLER` werden
- Wer Seller werden möchte, soll sich grundsätzlich mit einem neuen Account registrieren
- Alternativ kann ein Admin eine Rollenänderung durchführen

### Auswirkungen eines Rollenwechsels
Wenn ein Admin die Rolle eines Users ändert, verlieren bestehende rollenspezifische Daten ihre Gültigkeit.

Beispiele:
- Wechsel von `CUSTOMER` zu `SELLER`:
    - bisherige Bestellungen gehen verloren
    - bisheriger Warenkorb geht verloren
- Wechsel von `SELLER` zu `CUSTOMER`:
    - Shop-Bezug geht verloren
    - Produktbezug geht verloren
    - seller-spezifische Profildaten gehen verloren

### Fachliche Bewertung
Ein Rollenwechsel entspricht im System fachlich nahezu einer Neuerstellung des Nutzerprofils mit anderer Rolle.

---

## Adressmodell

Da Seller und Customer jeweils Adressen benötigen, sollte ein gemeinsames Address-Modell verwendet werden.

### Vorschlag für Address-Struktur

- `street`
- `houseNumber`
- `postalCode`
- `city`
- `country`

### Beispiel
```json
{
  "street": "Musterstraße",
  "houseNumber": "12a",
  "postalCode": "12345",
  "city": "Berlin",
  "country": "Deutschland"
}