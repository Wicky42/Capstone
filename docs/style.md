# style.md – Look & Feel / UI Style Guide

Projekt: **Nischen-Marktplatz für selbstgemachte Lebensmittel**  
Gewähltes Look & Feel: **Sanft & Premium**  
Gewählte Typografie: **Typografie 2 – Modern & Sympathisch**

---

## 1. Design-Ziel

Das UI soll wirken:

- modern
- sympathisch
- freundlich
- minimalistisch
- stilvoll
- hochwertig
- zugänglich
- kontraststark
- gut wartbar für ein React-/TypeScript-Frontend

Der Stil passt zu einem Marktplatz für handgemachte Lebensmittel, kleine Manufakturen, regionale Produkte, Geschenkboxen und hochwertige Nischenprodukte.

---

## 2. Farbpalette

### Primärpalette

| Rolle | Name | Hex | Verwendung |
|---|---:|---:|---|
| Primär | Schiefer | `#334155` | Header, Primärflächen, Navigation, starke UI-Elemente |
| Sekundär | Eukalyptus | `#84A98C` | Buttons, Chips, positive Akzente, natürliche UI-Elemente |
| Akzent | Sand | `#D4A373` | Highlights, Badges, kleine emotionale Details |
| Hintergrund | Warmes Off-White | `#FAF8F4` | App-Hintergrund, Seitenflächen |
| Fläche | Weiß | `#FFFFFF` | Cards, Modals, Formulare, Produktkarten |
| Text | Anthrazit | `#111827` | Haupttext, Headlines, Labels |

---

## 3. Erweiterte Farbskala

### Schiefer

| Token | Hex | Verwendung |
|---|---:|---|
| `slate-50` | `#F8FAFC` | sehr helle UI-Fläche |
| `slate-100` | `#F1F5F9` | dezente Trennflächen |
| `slate-200` | `#E2E8F0` | Borders |
| `slate-500` | `#64748B` | sekundärer Text |
| `slate-700` | `#334155` | Primärfarbe |
| `slate-900` | `#0F172A` | sehr dunkler Text / Kontrast |

### Eukalyptus

| Token | Hex | Verwendung |
|---|---:|---|
| `eucalyptus-50` | `#F2F7F4` | sanfte grüne Hintergründe |
| `eucalyptus-100` | `#E5EFE8` | Chips, Badges, Hover-Flächen |
| `eucalyptus-300` | `#B8CFBD` | dezente Akzente |
| `eucalyptus-500` | `#84A98C` | Sekundärfarbe / CTA |
| `eucalyptus-600` | `#6F9678` | CTA Hover |
| `eucalyptus-700` | `#577A61` | Text auf hellen Grünflächen |

### Sand

| Token | Hex | Verwendung |
|---|---:|---|
| `sand-50` | `#FBF4EC` | dezente warme Flächen |
| `sand-100` | `#F3E2CF` | Badges, Hinweise |
| `sand-300` | `#E2BC91` | Akzentflächen |
| `sand-500` | `#D4A373` | Akzentfarbe |
| `sand-600` | `#B98553` | Hover / aktive Akzente |
| `sand-700` | `#8F643C` | kontraststarker Akzenttext |

### Neutrale Farben

| Token | Hex | Verwendung |
|---|---:|---|
| `background` | `#FAF8F4` | App-Hintergrund |
| `surface` | `#FFFFFF` | Cards, Inputs, Modals |
| `surface-muted` | `#F6F1E9` | dezente Bereiche |
| `border` | `#E7E0D6` | Standard-Border |
| `border-strong` | `#CBD5E1` | stärkere Border |
| `text-primary` | `#111827` | primärer Text |
| `text-secondary` | `#4B5563` | sekundärer Text |
| `text-muted` | `#6B7280` | Meta-Text |
| `text-inverse` | `#FFFFFF` | Text auf dunklen Flächen |

---

## 4. Semantische Farben

| Rolle | Hex | Verwendung |
|---|---:|---|
| Erfolg | `#2F7D5C` | Erfolgsmeldungen, abgeschlossene Aktionen |
| Erfolg hell | `#E8F5EE` | Success Alert Background |
| Warnung | `#B7791F` | Warnhinweise |
| Warnung hell | `#FFF7E6` | Warning Alert Background |
| Fehler | `#B42318` | Fehler, destructive actions |
| Fehler hell | `#FDECEC` | Error Alert Background |
| Info | `#2563EB` | neutrale Systeminformationen |
| Info hell | `#EFF6FF` | Info Alert Background |

---

## 5. Kontrastregeln

Für gute Lesbarkeit und kontrastkonforme UI gelten diese Regeln:

| Kombination | Verwendung | Bewertung |
|---|---|---|
| `#111827` auf `#FFFFFF` | Haupttext auf Cards | sehr gut |
| `#111827` auf `#FAF8F4` | Haupttext auf Seitenhintergrund | sehr gut |
| `#FFFFFF` auf `#334155` | Text auf Primärbutton | sehr gut |
| `#FFFFFF` auf `#84A98C` | Text auf grünem Button | nur für größere / fette Button-Texte verwenden |
| `#111827` auf `#E5EFE8` | Text auf hellgrünem Chip | gut |
| `#111827` auf `#F3E2CF` | Text auf Sand-Badge | gut |
| `#6B7280` auf `#FFFFFF` | Meta-Text | sparsam verwenden |

**Empfehlung:** Für kleine Schrift immer `#111827`, `#334155` oder `#4B5563` verwenden. Sehr helle Akzentfarben nur als Hintergrund, nicht als Textfarbe.

---

## 6. Typografie

Gewählte Typografie: **Typografie 2 – Modern & Sympathisch**

### Font Pairing

| Bereich | Font | Einsatz |
|---|---|---|
| Headlines | `Manrope` | Seitentitel, Card-Titel, Hero-Texte |
| UI & Text | `Source Sans 3` | Fließtext, Buttons, Labels, Forms, Navigation |

### Google Fonts Import

```css
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@500;600;700;800&family=Source+Sans+3:wght@400;500;600;700&display=swap');
```

### CSS Font Tokens

```css
:root {
  --font-heading: 'Manrope', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  --font-body: 'Source Sans 3', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
```

---

## 7. Schriftgrößen

### Desktop

| Token | Größe | Line Height | Gewicht | Verwendung |
|---|---:|---:|---:|---|
| `display-xl` | `64px` | `1.05` | `800` | Hero Headlines |
| `display-lg` | `52px` | `1.08` | `800` | Landingpage Titel |
| `h1` | `40px` | `1.15` | `800` | Seitentitel |
| `h2` | `32px` | `1.2` | `700` | Abschnittstitel |
| `h3` | `24px` | `1.25` | `700` | Card-Gruppen |
| `h4` | `20px` | `1.3` | `700` | Produktkarten-Titel |
| `body-lg` | `18px` | `1.6` | `400` | größere Fließtexte |
| `body` | `16px` | `1.6` | `400` | Standardtext |
| `body-sm` | `14px` | `1.5` | `400` | Meta-Text |
| `label` | `14px` | `1.4` | `600` | Formularlabels |
| `caption` | `12px` | `1.4` | `600` | Badges, kleine Hinweise |
| `button` | `16px` | `1.2` | `700` | Buttons |

### Mobile

| Token | Größe | Line Height | Gewicht |
|---|---:|---:|---:|
| `display-xl` | `42px` | `1.1` | `800` |
| `display-lg` | `36px` | `1.12` | `800` |
| `h1` | `32px` | `1.15` | `800` |
| `h2` | `26px` | `1.2` | `700` |
| `h3` | `22px` | `1.25` | `700` |
| `h4` | `19px` | `1.3` | `700` |
| `body-lg` | `17px` | `1.55` | `400` |
| `body` | `16px` | `1.55` | `400` |
| `body-sm` | `14px` | `1.45` | `400` |

---

## 8. Typografie-Styles

```css
body {
  font-family: var(--font-body);
  font-size: 16px;
  line-height: 1.6;
  font-weight: 400;
  color: var(--color-text-primary);
  background: var(--color-background);
}

h1,
h2,
h3,
h4,
h5,
h6 {
  font-family: var(--font-heading);
  color: var(--color-text-primary);
  letter-spacing: -0.03em;
  margin: 0;
}

h1 {
  font-size: clamp(2rem, 5vw, 2.5rem);
  line-height: 1.15;
  font-weight: 800;
}

h2 {
  font-size: clamp(1.625rem, 4vw, 2rem);
  line-height: 1.2;
  font-weight: 700;
}

h3 {
  font-size: 1.5rem;
  line-height: 1.25;
  font-weight: 700;
}

p {
  margin: 0;
  font-size: 1rem;
  line-height: 1.6;
  color: var(--color-text-secondary);
}

small {
  font-size: 0.875rem;
  line-height: 1.5;
  color: var(--color-text-muted);
}
```

---

## 9. Link-Styles

Links sollen freundlich, klar und hochwertig wirken.

```css
a {
  color: var(--color-eucalyptus-700);
  font-weight: 600;
  text-decoration: none;
  text-underline-offset: 4px;
}

a:hover {
  color: var(--color-slate-700);
  text-decoration: underline;
}

a:focus-visible {
  outline: 3px solid var(--color-sand-300);
  outline-offset: 3px;
  border-radius: 6px;
}
```

### Beispiel

```tsx
<a href="/shops/honigstube">Mehr entdecken →</a>
```

---

## 10. Layout-Tokens

### Spacing

| Token | Wert |
|---|---:|
| `space-1` | `4px` |
| `space-2` | `8px` |
| `space-3` | `12px` |
| `space-4` | `16px` |
| `space-5` | `20px` |
| `space-6` | `24px` |
| `space-8` | `32px` |
| `space-10` | `40px` |
| `space-12` | `48px` |
| `space-16` | `64px` |
| `space-20` | `80px` |

### Radius

| Token | Wert | Verwendung |
|---|---:|---|
| `radius-sm` | `8px` | Inputs, kleine Badges |
| `radius-md` | `12px` | Buttons, Chips |
| `radius-lg` | `16px` | Cards |
| `radius-xl` | `24px` | große Sections |
| `radius-full` | `999px` | Pills, Avatare |

### Shadows

```css
--shadow-sm: 0 1px 2px rgba(17, 24, 39, 0.06);
--shadow-md: 0 8px 24px rgba(17, 24, 39, 0.08);
--shadow-lg: 0 20px 48px rgba(17, 24, 39, 0.12);
```

---

## 11. CSS Design Tokens

```css
:root {
  /* Fonts */
  --font-heading: 'Manrope', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  --font-body: 'Source Sans 3', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;

  /* Primary palette */
  --color-primary: #334155;
  --color-secondary: #84A98C;
  --color-accent: #D4A373;
  --color-background: #FAF8F4;
  --color-surface: #FFFFFF;
  --color-text-primary: #111827;

  /* Slate */
  --color-slate-50: #F8FAFC;
  --color-slate-100: #F1F5F9;
  --color-slate-200: #E2E8F0;
  --color-slate-500: #64748B;
  --color-slate-700: #334155;
  --color-slate-900: #0F172A;

  /* Eucalyptus */
  --color-eucalyptus-50: #F2F7F4;
  --color-eucalyptus-100: #E5EFE8;
  --color-eucalyptus-300: #B8CFBD;
  --color-eucalyptus-500: #84A98C;
  --color-eucalyptus-600: #6F9678;
  --color-eucalyptus-700: #577A61;

  /* Sand */
  --color-sand-50: #FBF4EC;
  --color-sand-100: #F3E2CF;
  --color-sand-300: #E2BC91;
  --color-sand-500: #D4A373;
  --color-sand-600: #B98553;
  --color-sand-700: #8F643C;

  /* Neutral */
  --color-surface-muted: #F6F1E9;
  --color-border: #E7E0D6;
  --color-border-strong: #CBD5E1;
  --color-text-secondary: #4B5563;
  --color-text-muted: #6B7280;
  --color-text-inverse: #FFFFFF;

  /* Semantic */
  --color-success: #2F7D5C;
  --color-success-bg: #E8F5EE;
  --color-warning: #B7791F;
  --color-warning-bg: #FFF7E6;
  --color-error: #B42318;
  --color-error-bg: #FDECEC;
  --color-info: #2563EB;
  --color-info-bg: #EFF6FF;

  /* Radius */
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --radius-full: 999px;

  /* Shadow */
  --shadow-sm: 0 1px 2px rgba(17, 24, 39, 0.06);
  --shadow-md: 0 8px 24px rgba(17, 24, 39, 0.08);
  --shadow-lg: 0 20px 48px rgba(17, 24, 39, 0.12);

  /* Motion */
  --transition-fast: 150ms ease;
  --transition-base: 220ms ease;
}
```

---

## 12. Buttons

### Primary Button

Für wichtigste Aktionen wie:

- In den Warenkorb
- Jetzt kaufen
- Checkout
- Speichern

```css
.button-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 48px;
  padding: 0 24px;
  border: 0;
  border-radius: var(--radius-md);
  background: var(--color-eucalyptus-500);
  color: var(--color-text-inverse);
  font-family: var(--font-body);
  font-size: 16px;
  font-weight: 700;
  line-height: 1.2;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition:
    background var(--transition-fast),
    transform var(--transition-fast),
    box-shadow var(--transition-fast);
}

.button-primary:hover {
  background: var(--color-eucalyptus-600);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.button-primary:active {
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

.button-primary:focus-visible {
  outline: 3px solid var(--color-sand-300);
  outline-offset: 3px;
}
```

### Dark Button

Für Hero-Aktionen oder Navigation.

```css
.button-dark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 48px;
  padding: 0 24px;
  border: 0;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--color-text-inverse);
  font-family: var(--font-body);
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}

.button-dark:hover {
  background: var(--color-slate-900);
}
```

### Secondary Button

```css
.button-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 44px;
  padding: 0 20px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-primary);
  font-family: var(--font-body);
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}

.button-secondary:hover {
  background: var(--color-eucalyptus-50);
  border-color: var(--color-eucalyptus-300);
}
```

---

## 13. Product Card

### Ziel

Die Produktkarte ist ein zentrales UI-Element für:

- Startseite
- Produktübersicht
- Shop-Seite
- Suchergebnisse
- Empfehlungen

### Style

```css
.product-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);
  transition:
    transform var(--transition-base),
    box-shadow var(--transition-base),
    border-color var(--transition-base);
}

.product-card:hover {
  transform: translateY(-3px);
  border-color: var(--color-eucalyptus-300);
  box-shadow: var(--shadow-md);
}

.product-card__image {
  aspect-ratio: 4 / 3;
  width: 100%;
  object-fit: cover;
  background: var(--color-surface-muted);
}

.product-card__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}

.product-card__link {
  color: var(--color-eucalyptus-700);
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
}

.product-card__link:hover {
  text-decoration: underline;
}

.product-card__title {
  font-family: var(--font-heading);
  font-size: 24px;
  line-height: 1.2;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--color-text-primary);
}

.product-card__description {
  font-family: var(--font-body);
  font-size: 16px;
  line-height: 1.55;
  color: var(--color-text-secondary);
}

.product-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-muted);
  font-size: 15px;
}

.product-card__price {
  font-family: var(--font-heading);
  font-size: 28px;
  line-height: 1.1;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--color-text-primary);
}
```

### JSX-Beispiel

```tsx
<article className="product-card">
  <img
    className="product-card__image"
    src="/static/images/aprikosenkonfituere.jpg"
    alt="Aprikosenkonfitüre im Glas"
  />

  <div className="product-card__content">
    <a className="product-card__link" href="/shops/manufaktur-sonnenglas">
      Mehr entdecken →
    </a>

    <h3 className="product-card__title">
      Aprikosenkonfitüre
    </h3>

    <p className="product-card__description">
      Natürlich süß, fein abgestimmt und perfekt für Frühstück oder Geschenkboxen.
    </p>

    <div className="product-card__meta">
      <span>220 g</span>
      <span>•</span>
      <span>Regional</span>
    </div>

    <strong className="product-card__price">
      6,90 €
    </strong>

    <button className="button-primary">
      Jetzt kaufen →
    </button>
  </div>
</article>
```

---

## 14. Chips & Badges

### Chip

```css
.chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 32px;
  padding: 0 12px;
  border-radius: var(--radius-full);
  background: var(--color-eucalyptus-100);
  color: var(--color-eucalyptus-700);
  font-size: 14px;
  font-weight: 700;
}
```

### Badge

```css
.badge {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: var(--radius-full);
  background: var(--color-sand-100);
  color: var(--color-sand-700);
  font-size: 13px;
  font-weight: 700;
}
```

---

## 15. Forms

```css
.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 700;
}

.input,
.textarea,
.select {
  width: 100%;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-primary);
  font-family: var(--font-body);
  font-size: 16px;
  line-height: 1.4;
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast);
}

.input,
.select {
  min-height: 46px;
  padding: 0 14px;
}

.textarea {
  min-height: 120px;
  padding: 12px 14px;
  resize: vertical;
}

.input::placeholder,
.textarea::placeholder {
  color: var(--color-text-muted);
}

.input:focus,
.textarea:focus,
.select:focus {
  outline: none;
  border-color: var(--color-eucalyptus-500);
  box-shadow: 0 0 0 3px rgba(132, 169, 140, 0.24);
}
```

---

## 16. Navigation

```css
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  padding: 0 32px;
  background: rgba(250, 248, 244, 0.88);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--color-border);
}

.navbar__brand {
  font-family: var(--font-heading);
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--color-primary);
}

.navbar__link {
  color: var(--color-text-secondary);
  font-size: 16px;
  font-weight: 700;
  text-decoration: none;
}

.navbar__link:hover {
  color: var(--color-text-primary);
}
```

---

## 17. Page Layout

```css
.page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(132, 169, 140, 0.18), transparent 32rem),
    var(--color-background);
  color: var(--color-text-primary);
}

.container {
  width: min(100% - 32px, 1180px);
  margin-inline: auto;
}

.section {
  padding-block: 64px;
}

.section-header {
  max-width: 720px;
  margin-bottom: 32px;
}

.section-kicker {
  margin-bottom: 10px;
  color: var(--color-eucalyptus-700);
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.section-title {
  font-family: var(--font-heading);
  font-size: clamp(2rem, 5vw, 3.25rem);
  line-height: 1.08;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.section-description {
  margin-top: 16px;
  color: var(--color-text-secondary);
  font-size: 18px;
  line-height: 1.65;
}
```

---

## 18. Grid für Produktübersichten

```css
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

@media (max-width: 960px) {
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
```

---

## 19. Accessibility-Regeln

### Fokus

Alle interaktiven Elemente brauchen einen sichtbaren Fokuszustand.

```css
:focus-visible {
  outline: 3px solid var(--color-sand-300);
  outline-offset: 3px;
}
```

### Mindestgrößen

| Element | Mindestgröße |
|---|---:|
| Button | `44px` Höhe |
| Icon Button | `44px × 44px` |
| Input | `44px` Höhe |
| Checkbox / Radio Touch Area | `44px × 44px` |

### Text

- Kein Fließtext unter `16px`
- Meta-Text maximal sparsam in `14px`
- Keine rein farbliche Unterscheidung von Status
- Links zusätzlich durch Hover/Underline erkennbar machen
- Fehler mit Text + Farbe darstellen

---

## 20. Bildsprache

Produktbilder sollen wirken:

- hell
- natürlich
- weich
- hochwertig
- handgemacht
- nicht überinszeniert

Empfohlene Motive:

- Gläser mit Stoffdeckel
- Holz, Leinen, Keramik
- dezente Pflanzen / Kräuter
- natürliche Tageslicht-Szenen
- warme, ruhige Hintergründe

Nicht verwenden:

- harte Schatten
- überladene Food-Fotografie
- grelle Farben
- sterile Stock-Optik
- unruhige Hintergründe

---

## 21. Komponenten-Priorität für MVP

Für das MVP sollten zuerst diese Komponenten mit den Styles umgesetzt werden:

1. `Button`
2. `ProductCard`
3. `ShopCard`
4. `Navbar`
5. `Input`
6. `SearchField`
7. `FilterChip`
8. `Badge`
9. `Alert`
10. `PageLayout`

---

## 22. Empfohlene Datei-Struktur im Frontend

```text
src/
├── styles/
│   ├── globals.css
│   ├── tokens.css
│   ├── typography.css
│   ├── components.css
│   └── utilities.css
│
├── components/
│   ├── shared/
│   │   ├── Button.tsx
│   │   ├── Badge.tsx
│   │   ├── Chip.tsx
│   │   └── Card.tsx
│   └── product/
│       └── ProductCard.tsx
```

---

## 23. Kurzentscheidung

Die finale Style-Richtung lautet:

```text
Palette:
Sanft & Premium

Primär:
#334155

Sekundär:
#84A98C

Akzent:
#D4A373

Hintergrund:
#FAF8F4

Typografie:
Headline: Manrope
UI & Text: Source Sans 3

UI-Wirkung:
modern, sympathisch, freundlich, hochwertig, minimalistisch
```
