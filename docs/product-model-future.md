# Product Model – Future Extensions

## Überblick

Dieses Dokument beschreibt mögliche zukünftige Erweiterungen des Product Models.
Diese Funktionen sind **nicht Bestandteil des MVP**, wurden jedoch frühzeitig identifiziert, um die Architektur entsprechend vorbereiten zu können.

Ziel ist es, spätere Erweiterungen ohne grundlegende Änderungen am bestehenden System zu ermöglichen.

---

## Erweiterungen der Produktstruktur

### Mehrere Kategorien pro Produkt

#### Beschreibung

Ein Produkt kann aktuell genau einer Kategorie zugeordnet werden.
Zukünftig soll ein Produkt mehreren Kategorien zugeordnet werden können.

#### Beispiel

* „Honig“ → Kategorien:

    * Lebensmittel
    * Süßwaren
    * Regionalprodukte

#### Technische Anpassung

* `category` → wird zu `categories[]`

---

### Produktvarianten

#### Beschreibung

Ein Produkt kann mehrere Varianten besitzen, z. B.:

* Größe (250g / 500g / 1kg)
* Geschmacksrichtung
* Verpackungsart

#### Beispiel

```json
{
  "name": "Waldhonig",
  "variants": [
    { "size": "250g", "price": 4.99 },
    { "size": "500g", "price": 8.99 }
  ]
}
```

#### Auswirkungen

* Preis und Bestand müssen auf Variantenebene geführt werden
* Warenkorb und Bestellungen müssen Varianten unterstützen

---

### Erweiterte Produktdaten

#### Nährwertangaben

* Kalorien
* Fett
* Kohlenhydrate
* Eiweiß

#### Zutatenliste

* strukturierte Angabe von Inhaltsstoffen

#### Allergene

* Kennzeichnung von Allergenen

#### Herkunft

* Region / Herkunft der Zutaten

---

### Erweiterte Bilderlogik

#### Beschreibung

* Bilder können zusätzlich Metadaten enthalten:

    * Beschreibung
    * Alt-Text (für SEO und Barrierefreiheit)
    * Zoom- oder Detailansichten

#### Erweiterungen

* Bildergalerien
* Reihenfolge / Sortierung
* ggf. Videos

---

## Produktstatus und Lifecycle-Erweiterungen

### Erweiterte Statuswerte

* `ARCHIVED`
* `OUT_OF_STOCK`

### Rückruf-Logik

#### Beschreibung

Der Status `RECALLED` wird detaillierter ausgestaltet.

#### Mögliche Erweiterungen

* Rückrufgrund (`recallReason`)
* Rückrufdatum
* betroffene Chargen
* Sichtbarkeit für Kunden
* automatische Benachrichtigung betroffener Käufer

---

## Bestandsmanagement

### Erweiterungen

* Mindestbestand (Warnung)
* automatische Benachrichtigung bei niedrigem Bestand
* Reservierung von Bestand während Checkout
* getrennte Lagerbestände (z. B. Seller vs. Zentrallager)

---

## Preislogik

### Erweiterungen

* Rabattpreise
* zeitlich begrenzte Aktionen
* Staffelpreise
* Gutscheine

---

## SEO und Auffindbarkeit

### Erweiterungen

* SEO-Titel
* SEO-Beschreibung
* URL-Slug
* strukturierte Daten für Suchmaschinen

---

## Tags und Suche

### Beschreibung

Produkte können mit Tags versehen werden:

* „Bio“
* „Vegan“
* „Glutenfrei“
* „Regional“

### Vorteile

* bessere Filterung
* bessere Suche
* bessere Kategorisierung

---

## Produktbewertungen

### Beschreibung

Customers können Produkte bewerten.

### Mögliche Daten

* Sternebewertung (1–5)
* Kommentar
* Datum
* Referenz zum Customer

### Erweiterungen

* Durchschnittsbewertung
* Anzeige im Produktlisting

---

## Personalisierung

### Erweiterungen

* empfohlene Produkte
* „Ähnliche Produkte“
* „Kunden kauften auch“

---

## Technische Überlegungen

Diese Erweiterungen haben Auswirkungen auf:

* Datenbankstruktur (MongoDB Dokumente vs. Referenzen)
* API-Design
* Frontend-Komplexität
* Performance (z. B. Suche, Filter)

### Empfehlung

* Schon im MVP flexible Strukturen verwenden (z. B. Arrays statt Einzelwerte)
* Status statt Boolean-Felder nutzen
* Erweiterbarkeit der Datenstruktur berücksichtigen

---

## Priorisierung (optional)

Diese Erweiterungen können später priorisiert werden:

### High Priority (früh nach MVP)

* mehrere Kategorien
* Tags
* bessere Suche

### Medium Priority

* Varianten
* Bewertungen
* SEO

### Low Priority

* Personalisierung
* komplexe Preislogik
* detaillierte Rückrufsysteme
