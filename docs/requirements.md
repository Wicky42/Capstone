# Requirements

## Zweck

Dieses Dokument beschreibt die fachlichen und technischen Anforderungen des Systems für einen spezialisierten Online-Marktplatz für selbstgemachte Lebensmittel.

Das System unterstützt drei Rollen:

- `ADMIN`
- `SELLER`
- `CUSTOMER`

Ziel dieses Dokuments ist die Definition der Anforderungen für den ersten nutzbaren Release (MVP / Version 1) sowie die Abgrenzung späterer Erweiterungen.

---

## Systemkontext

Die Plattform ermöglicht es Sellern, selbstgemachte Lebensmittel online anzubieten, ohne einen eigenen Onlineshop betreiben zu müssen. Customers können Produkte aus verschiedenen Shops auf einem gemeinsamen Marktplatz durchsuchen, filtern, in den Warenkorb legen und bestellen.

Die Bestellung wird aus Kundensicht als Gesamtbestellung behandelt.  
Intern wird sie in seller-spezifische Teilbestellungen aufgeteilt.

---

## Rollen

### CUSTOMER
Kauft Produkte auf dem Marktplatz und verwaltet den eigenen Warenkorb sowie die eigenen Bestellungen.

### SELLER
Verwaltet genau einen Shop und verkauft Produkte über die Plattform.

### ADMIN
Verwaltet Nutzer, Rollen, Produkte, Bestellungen und Plattformdaten.

---

# MVP / Version 1 Requirements

## 1. Authentifizierung und Registrierung

### Funktionale Anforderungen
- Das System muss eine Authentifizierung über `GitHub OAuth` unterstützen.
- Vor dem OAuth-Login muss ein Nutzer auswählen, ob er sich als `SELLER` oder `CUSTOMER` registrieren möchte.
- Das System muss jedem User genau eine Rolle zuweisen.
- Das System muss pro User genau einen OAuth-Account verwalten.
- Das System muss initial mindestens einen Admin unterstützen.
- Das System muss ermöglichen, dass ein bestehender Admin weitere Nutzer zu Admins machen kann.

### Fachliche Regeln
- Eine Selbstregistrierung als `ADMIN` ist nicht erlaubt.
- Ein User kann nur genau eine Rolle besitzen.
- Ein bestehender `CUSTOMER` kann sich nicht regulär selbst in einen `SELLER` umwandeln.
- Ein Rollenwechsel ist nur durch einen Admin möglich.
- Bei einem Rollenwechsel verlieren rollenspezifische Daten ihre fachliche Gültigkeit.

---

## 2. Admin-Verwaltung

### Funktionale Anforderungen
- Ein `ADMIN` muss alle in der Datenbank vorhandenen User einsehen können.
- Ein `ADMIN` muss für jeden User die aktuelle Rolle sehen können.
- Ein `ADMIN` muss die Rolle eines Users ändern können.
- Ein `ADMIN` muss weitere Admins festlegen können.

### Fachliche Regeln
- Der initiale Admin wird systemseitig vorgegeben.
- Weitere Admins können nur durch einen bestehenden Admin bestimmt werden.

---

## 3. Seller-Onboarding

### Funktionale Anforderungen
- Ein `SELLER` muss nach der Registrierung ein Onboarding durchlaufen.
- Das Onboarding muss mindestens die Erstellung eines Shops ermöglichen.
- Das Onboarding muss verlangen, dass mindestens ein Produkt angelegt wird.

### Fachliche Regeln
- Ein Shop darf erst sichtbar werden, wenn:
    - der Seller registriert ist
    - der Seller bestätigt/freigeschaltet ist
    - das Shop-Onboarding abgeschlossen wurde
    - mindestens ein Produkt vorhanden ist
- Shops ohne Produkte dürfen nicht sichtbar sein.

---

## 4. Shop-Verwaltung

### Funktionale Anforderungen
- Ein `SELLER` muss genau einen Shop besitzen können.
- Ein `SELLER` muss seinen Shop aufbauen und verwalten können.
- Ein `SELLER` muss grundlegende Shop-Daten pflegen können.

### Fachliche Regeln
- Ein Seller besitzt genau einen Shop.
- Ein Shop gehört genau einem Seller.
- Hat ein Shop keine Produkte, darf er auf der Startseite nicht angezeigt werden.

### MVP-Hinweis
- Erweiterte Design-Anpassungen des Shops sind nicht Kernbestandteil von Version 1 und können später ausgebaut werden.

---

## 5. Produktverwaltung

### Funktionale Anforderungen
- Ein `SELLER` muss Produkte anlegen können.
- Für das Anlegen eines Produkts muss eine eigene Create-Product-Seite existieren.
- Ein `SELLER` muss Produkte bearbeiten können.
- Ein `SELLER` muss Produkte löschen können.
- Ein `ADMIN` muss Produkte systemweit verwalten können.
- Ein `ADMIN` muss Produkte aus dem gesamten System entfernen können.
- Ein `ADMIN` muss Produktrückrufe starten können.

### Pflichtfelder eines Produkts
Die folgenden Felder sind im MVP Pflichtfelder:

- `image`
- `price`
- `description`
- `bestBeforeDate` (Mindesthaltbarkeitsdatum)
- `productionDate` (Produktionsdatum)

### Weitere Produktattribute
- Produktkategorie muss unterstützt werden
- Produktname sollte als Pflichtfeld geführt werden

### Fachliche Regeln
- Produkte dürfen nur angelegt werden, wenn alle Pflichtfelder gesetzt sind.
- Produkte eines Sellers ohne abgeschlossenes Onboarding dürfen nicht öffentlich sichtbar sein.

---

## 6. Produktanzeige, Suche und Filter

### Funktionale Anforderungen
- Ein `CUSTOMER` muss auf der Startseite alle sichtbaren Produkte aller sichtbaren Shops sehen können.
- Ein `CUSTOMER` muss Produkte nach Kategorien filtern können.
- Das System muss eine Suchfunktion über ein Textfeld bereitstellen.
- Ein `CUSTOMER` muss passende Produkte aus allen Shops als Suchergebnis angezeigt bekommen.
- Ein `CUSTOMER` muss eine Produktdetailseite aufrufen können.

### UI-Anforderungen
- Die Filteroption soll auf der Startseite sichtbar sein.
- Die Filteroption kann entweder:
    - linksseitig auf der Seite
    - oder unterhalb des Headers
      platziert werden.
- Das Suchfeld soll auf der Startseite verfügbar sein.

### Fachliche Regeln
- Es dürfen nur Produkte aus sichtbaren Shops angezeigt werden.
- Shops ohne Produkte werden auf der Startseite nicht angezeigt.

---

## 7. Navigation zu Shops

### Funktionale Anforderungen
- Von der Produktdetailseite muss ein direkter Link zum zugehörigen Shop vorhanden sein.
- Das System soll zusätzliche Möglichkeiten bieten, direkt auf einen Shop eines Sellers zu gelangen.

### Fachliche Regeln
- Ein Shop ist nur dann direkt erreichbar, wenn er sichtbar/freigeschaltet ist und Produkte besitzt.

### Offene Details
- Welche zusätzlichen Einstiegspunkte zum Shop im MVP enthalten sind, muss im UI-Konzept noch konkretisiert werden.

---

## 8. Warenkorb

### Funktionale Anforderungen
- Ein `CUSTOMER` muss Produkte in den Warenkorb legen können.
- Ein `CUSTOMER` muss mehrere Exemplare desselben Produkts in den Warenkorb legen können.
- Ein `CUSTOMER` muss Produkte aus verschiedenen Shops in einem gemeinsamen Warenkorb sammeln können.
- Ein `CUSTOMER` muss den aktuellen Warenkorb einsehen können.

### Fachliche Regeln
- Ein Customer besitzt genau einen aktiven Warenkorb.
- Ein Warenkorb darf Produkte mehrerer Seller enthalten.

---

## 9. Bestellung

### Funktionale Anforderungen
- Ein `CUSTOMER` muss eine Bestellung aus dem Warenkorb aufgeben können.
- Das System muss aus einem Warenkorb eine Kunden-Gesamtbestellung erzeugen.
- Das System muss die Gesamtbestellung intern in seller-spezifische Teilbestellungen aufteilen.
- Ein `CUSTOMER` muss die eigene Bestellung einsehen können.
- Ein `SELLER` muss nur die Bestellungen sehen können, die die eigenen Produkte betreffen.
- Ein `ADMIN` muss die vollständige Bestellung einsehen können.

### Fachliche Regeln
- Der Customer sieht die Bestellung als Gesamtbestellung.
- Seller sehen nur ihren jeweiligen Bestellanteil.
- Der Admin sieht die vollständige Bestellung inklusive aller beteiligten Seller.

---

## 10. Rechnungen

### Funktionale Anforderungen
- Das System muss beim Kaufabschluss automatisch eine Rechnung für den Customer erzeugen.
- Der Customer muss eine Rechnung über den gesamten Bestellwert erhalten.
- Die seller-spezifischen Teilbestellungen müssen intern erzeugt und den jeweiligen Sellern zugeordnet werden.

### Fachliche Regeln
- Der Customer erhält genau eine Gesamtrechnung pro Bestellung.
- Seller sehen keine Gesamtrechnung des Customers, sondern nur die für sie relevanten Bestellinformationen.

---

## 11. Zahlungsstatus

### Funktionale Anforderungen
- Eine Bestellung muss ein Zahlungskennzeichen `isPaid` besitzen.

### Fachliche Regeln
- Im MVP wird `isPaid` automatisch auf `true` gesetzt, sobald eine Bestellung eingeht.
- Eine separate Zahlungsprüfung findet im MVP noch nicht statt.
- Die erste unterstützte Zahlungslogik entspricht damit einer vereinfachten Systemannahme.

### MVP-Hinweis
- Diese Logik dient nur der Vereinfachung in Version 1.
- Eine echte Zahlungsabwicklung folgt später.

---

## 12. Seller-Funktionen im MVP

Ein Seller muss in Version 1 mindestens Folgendes können:

- eigenen Shop aufbauen
- Onboarding abschließen
- Produkte anlegen
- Produkte bearbeiten
- Produkte löschen
- eigene Bestellungen einsehen

---

## 13. Customer-Funktionen im MVP

Ein Customer muss in Version 1 mindestens Folgendes können:

- alle sichtbaren Produkte sehen
- Produkte nach Kategorien filtern
- Produkte per Suchtext suchen
- Produktdetails ansehen
- Produkte zum Warenkorb hinzufügen
- mehrere Mengen eines Produkts in den Warenkorb legen
- eine Bestellung aufgeben
- eigene Rechnung einsehen

---

## 14. Admin-Funktionen im MVP

Ein Admin muss in Version 1 mindestens Folgendes können:

- alle User sehen
- Rollen aller User sehen
- Rollen ändern
- weitere Admins festlegen
- Produkte systemweit verwalten
- Produktrückrufe starten
- Produkte entfernen
- Gesamtbestellungen einsehen

---

# Nicht-funktionale Anforderungen

## 1. Architektur
- Das System soll als modernes Fullstack-Projekt umgesetzt werden.
- Das Backend soll mit `Spring Boot` und `Java` umgesetzt werden.
- Das Frontend soll mit `React` und `TypeScript` umgesetzt werden.
- Die Datenhaltung soll in `MongoDB` erfolgen.
- Das System soll einer `Layered Architecture` folgen.
- Die Umsetzung soll sich an Clean-Code-Prinzipien orientieren.

## 2. Wartbarkeit
- Der Code soll leicht wartbar sein.
- Verantwortlichkeiten sollen klar getrennt sein.
- Fachlogik soll sauber von Infrastruktur und UI getrennt sein.
- Komponenten und Module sollen verständlich aufgebaut sein.

## 3. Erweiterbarkeit
- Weitere Zahlungsanbieter sollen später leicht integrierbar sein.
- Weitere Auth-Provider sollen später ergänzbar sein.
- Zusätzliche Shop- und Produktfunktionen sollen ohne grundlegenden Architekturumbau möglich sein.

## 4. Sicherheit
- Geschützte Bereiche müssen rollenbasiert abgesichert sein.
- Seller dürfen nur ihre eigenen Produkte, Shops und Bestellungen verwalten.
- Customer dürfen nur ihre eigenen Bestellungen und Profildaten sehen.
- Admins dürfen systemweite Daten einsehen und verwalten.

## 5. Validierung
- Pflichtfelder müssen serverseitig validiert werden.
- Nur Händler aus Deutschland dürfen sich registrieren.
- Steuer-ID und Rechnungsdaten müssen formal validiert werden.
- E-Mail-Adressen müssen formal validiert werden.

---

# Fachliche Regeln / Business Rules

- Ein User hat genau eine Rolle.
- Ein Seller hat genau einen Shop.
- Ein Shop ist nur sichtbar, wenn das Onboarding abgeschlossen ist und mindestens ein Produkt existiert.
- Shops ohne Produkte werden nicht angezeigt.
- Ein Customer kann Produkte mehrerer Seller in einem Warenkorb sammeln.
- Eine Kundenbestellung wird intern in seller-spezifische Teilbestellungen aufgeteilt.
- Seller sehen nur ihren eigenen Bestellanteil.
- Der Admin sieht die vollständige Bestellung.
- Im MVP wird `isPaid` automatisch auf `true` gesetzt.
- Nur Händler aus Deutschland dürfen sich registrieren.
- Ein Admin kann weitere Admins bestimmen.
- Rollenwechsel sind nur administrativ möglich.

---

# Post-MVP Requirements

Die folgenden Anforderungen sind sinnvoll, aber nicht zwingend Bestandteil von Version 1.

## Zahlung
- Unterstützung echter Zahlungsabwicklung
- Integration externer Zahlungsanbieter wie `PayPal`
- Saubere Zahlungsstatus-Verarbeitung

## Seller-Abrechnung
- Monatliche Übersicht über verkaufte Waren
- Berechnung der Provisionen
- Abrechnungsübersicht für Seller
- Verwaltungsübersicht für Admins

## Shop
- Erweiterte Design-Anpassungen für Shops
- individuelles Look & Feel
- Header-Bild und Farbanpassungen in größerem Umfang

## Customer Experience
- erweiterte Shop-Navigation
- bessere Such- und Filtermöglichkeiten
- Geschenk- und Paketlogik weiter ausbauen

## Plattform
- weitere OAuth-Provider
- tiefere Fulfillment- und Lagerlogik
- zusätzliche Reporting- und Verwaltungsfunktionen

---

# MVP-Zusammenfassung

Version 1 soll folgende Kernfunktionen stabil bereitstellen:

- GitHub OAuth Login
- Rollenmodell mit `ADMIN`, `SELLER`, `CUSTOMER`
- Admin-Verwaltung von Usern und Rollen
- Seller-Onboarding
- Shop-Erstellung
- Produktanlage, Bearbeitung und Löschung
- Produktanzeige auf der Startseite
- Kategorienfilter
- Produktsuche
- Produktdetailseite
- Link vom Produkt zum Shop
- Warenkorb
- Bestellung
- automatische Kundenrechnung
- interne seller-spezifische Teilbestellungen
- vereinfachter Zahlungsstatus mit `isPaid = true`