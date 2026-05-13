import "./RegisterPage.css"
import { authService, type UserRole } from "../../services/authService";


export default function RegisterPage() {
    const handleRegisterClick = (role: UserRole) => {
        localStorage.setItem("selectedRole", role);
        authService.startGithubLogin();
    };

    const sellerBenefits = [
        "Deutschlandweit verkaufen, ohne eigenen Onlineshop aufbauen zu müssen",
        "Eigener Shop mit Produkten, Beschreibung, Bildern und Kategorien",
        "Weniger Aufwand, weil die Plattform Sichtbarkeit, Checkout und Rechnungen mitträgt",
        "Bestellungen für den eigenen Shop zentral einsehen und verwalten",
        "Ideal für kleine Manufakturen und regionale Anbieter mit wenig Technik- oder Marketingaufwand",
        "Nach dem Onboarding direkt mit dem Verkauf starten"
    ];

    const customerBenefits = [
        "Produkte aus verschiedenen Shops an einem Ort entdecken",
        "Alle sichtbaren Produkte durchsuchen, filtern und vergleichen",
        "Mehrere Produkte aus unterschiedlichen Shops in einem gemeinsamen Warenkorb sammeln",
        "Bestellungen einfach als Gesamtbestellung aufgeben",
        "Eigene Bestellungen und Rechnungen jederzeit einsehen",
        "Perfekt für hochwertige, handgemachte Lebensmittel und Geschenkideen"
    ];

    return (
        <section className="registerpage">
            <div className="registerpage__intro">
                <h2>Jetzt registrieren und direkt loslegen</h2>
                <p>
                    Wähle die passende Rolle und profitiere sofort von den Vorteilen
                    unseres spezialisierten Marktplatzes für selbstgemachte Lebensmittel.
                </p>
            </div>

            <div className="registerpage__grid">
                <div className="register__col register__card">
                    <span className="register__badge">Für Händler</span>
                    <h3>Als Händler registrieren</h3>
                    <p className="register__subtitle">
                        Verkaufe deine Produkte professionell, ohne einen eigenen Shop
                        entwickeln und vermarkten zu müssen.
                    </p>

                    <ul className="register__benefits">
                        {sellerBenefits.map((benefit) => (
                            <li key={benefit}>{benefit}</li>
                        ))}
                    </ul>

                    <p className="register__highlight">
                        Starte jetzt deinen Shop und erreiche neue Kunden auf einer
                        spezialisierten Plattform.
                    </p>

                    <button
                        type="button"
                        className="register__button register__button--seller"
                        onClick={() => handleRegisterClick("SELLER")}
                    >
                        Als Händler registrieren
                    </button>
                </div>

                <div className="register__col register__card">
                    <span className="register__badge">Für Kunden</span>
                    <h3>Als Kunde registrieren</h3>
                    <p className="register__subtitle">
                        Entdecke besondere Produkte kleiner Anbieter und bestelle bequem
                        über einen zentralen Marktplatz.
                    </p>

                    <ul className="register__benefits">
                        {customerBenefits.map((benefit) => (
                            <li key={benefit}>{benefit}</li>
                        ))}
                    </ul>

                    <p className="register__highlight">
                        Registriere dich jetzt und finde hochwertige, handgemachte Produkte
                        aus verschiedenen Shops an einem Ort.
                    </p>

                    <button
                        type="button"
                        className="register__button register__button--customer"
                        onClick={() => handleRegisterClick("CUSTOMER")}
                    >
                        Als Kunde registrieren
                    </button>
                </div>
            </div>
        </section>
    );
}