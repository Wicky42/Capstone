# Authentifizierung & Autorisierung

## Überblick

Das System verwendet:

* GitHub OAuth2
* Session-basierte Authentifizierung
* Rollenmodell (`ADMIN`, `SELLER`, `CUSTOMER`)

---

## Login Flow

1. User wählt Rolle (RegisterPage)
2. Rolle wird temporär gespeichert (LocalStorage oder Cookie)
3. Redirect zu GitHub OAuth
4. GitHub authentifiziert
5. Backend erstellt oder lädt User
6. Session wird erstellt
7. Redirect je Rolle:

    * CUSTOMER → `/`
    * SELLER → `/seller/onboarding`

---

## User-Erstellung

Beim ersten Login:

* User wird erstellt
* Rolle wird aus vorheriger Auswahl übernommen
* Seller startet mit `onboardingCompleted = false`

---

## Session

* Cookie-basiert
* Backend hält Session
* `/api/auth/me` liefert Userdaten

---

## Rollen

| Rolle    | Zugriff      |
| -------- | ------------ |
| ADMIN    | alles        |
| SELLER   | eigene Daten |
| CUSTOMER | eigene Daten |

---

## Sicherheitsregeln

* Seller darf nur eigene Produkte sehen
* Customer sieht keine SellerOrders
* Admin sieht alles

---

## Admin-Erstellung

* Initialer Admin wird beim Start gesetzt
* Weitere Admins nur durch Admin möglich

---

## Erweiterungen (später)

* JWT statt Session
* mehrere OAuth Provider
* Account Linking
