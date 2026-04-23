# Produktidee: Nischen-Marktplatz für selbstgemachte Lebensmittel

## Überblick

Ein spezialisierter Online-Marktplatz für selbstgemachte Lebensmittel, der kleine Händler dabei unterstützt, ihre Produkte deutschlandweit zu verkaufen – ohne eigenen Onlineshop und ohne starke Konkurrenz durch große Plattformen.

## Problem

* Kleine Händler (z. B. Imker, Manufakturen) verkaufen oft nur lokal
* Eigene Onlineshops bedeuten:

    * hoher technischer Aufwand
    * Marketingkosten
    * geringe Sichtbarkeit
* Große Plattformen wie Amazon oder eBay:

    * hohe Konkurrenz
    * hohe Provisionsgebühren
    * wenig Differenzierung für Nischenprodukte

## Zielgruppe

### Händler

* Produzenten von selbstgemachten Lebensmitteln (z. B. Honig, Marmelade, Gewürze)
* Kleine Manufakturen und lokale Anbieter
* Personen ohne eigenen Onlineshop oder Marketing-Know-how

### Käufer

* Menschen, die hochwertige, handgemachte Produkte suchen
* Käufer, die kleine Händler unterstützen möchten
* Personen, die Geschenkpakete zusammenstellen wollen

## Lösung

Eine zentrale Plattform, auf der:

* Händler eigene Shops erstellen können
* Produkte einfach eingestellt und verwaltet werden können
* Käufer Produkte aus verschiedenen Shops kombinieren können

Die Plattform übernimmt:

* Sichtbarkeit und Marketing
* Infrastruktur (Shop, Checkout, Rechnungen)
* Aggregation von Produkten in einem gemeinsamen Warenkorb

## Kernfunktionen

### Für Händler

* Registrierung und Login (OAuth2)
* Eigenen Shop erstellen
* Produkte verwalten:

    * Titel
    * Beschreibung
    * Preis
    * Bilder
    * Kategorien
* Shop anpassen:

    * Header-Bild
    * einfache Farbanpassungen (Look & Feel)
* Bestellungen einsehen
* Automatische Rechnungen erhalten

### Für Käufer

* Produkte über alle Shops durchsuchen
* Warenkorb mit Produkten verschiedener Händler
* Bestellung aufgeben
* Lieferung als kombinierbares Geschenkpaket
* Automatische Rechnungen erhalten

### Für Admins

* Übersicht über:

    * Händler
    * Käufer
    * Bestellungen
* Einsicht in alle Transaktionen
* Verwaltung von Rechnungen
* Kontrolle über Plattformdaten

## MVP (Minimum Viable Product)

* OAuth2 Login (Händler & Käufer)
* Händler können:

    * Shop erstellen
    * Produkte anlegen
* Käufer können:

    * Produkte sehen
    * Warenkorb nutzen
    * Bestellung abschließen
* Einfaches Admin-Panel:

    * Nutzerübersicht
    * Bestellübersicht
* Automatische Rechnungserstellung (Basisversion)

## Vision

* Aufbau eines spezialisierten Marktplatzes für hochwertige Nischenprodukte
* Fokus auf Qualität statt Masse
* Unterstützung lokaler Produzenten
* Etablierung als Alternative zu großen Plattformen
* Ausbau zu weiteren Nischen (z. B. Handwerk, DIY-Produkte)

## Abgrenzung zu bestehenden Lösungen

* Fokus auf **selbstgemachte Lebensmittel**
* Geringere Provisionen als große Plattformen
* Weniger Konkurrenz für Händler
* Kuratierte Plattform statt Massenmarkt
* Multi-Shop-Warenkorb mit Geschenkfokus

## Monetarisierung

* Provisionsmodell (geringer als große Marktplätze)
* Optional:

    * Premium-Features für Händler
    * Hervorgehobene Platzierungen

## Technische Ausrichtung (High-Level)

* Backend:

    * Java mit Spring Boot
    * OAuth2 Authentication
* Frontend:

    * React mit TypeScript
* Datenbank:

    * MongoDB
* Architektur:

    * Layered Architecture
    * Clean Code Prinzipien

## Besondere Mehrwerte

* Unterstützung kleiner Händler
* Einheitliches Einkaufserlebnis über mehrere Shops
* Geschenk-Use-Case (kuratierte Warenkörbe)
* Fokus auf Qualität und Handarbeit statt Massenware
