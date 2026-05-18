import "./LogoutPage.css";

export default function LogoutPage() {
    return (
        <section className="logoutpage">
            <div className="logoutpage__card">
                <h2>Auf Wiedersehen!</h2>
                <p>Du wurdest erfolgreich ausgeloggt.</p>
                <a
                    href="https://github.com/logout"
                    target="_blank"
                    rel="noreferrer"
                    className="button-primary"
                >
                    Auch bei GitHub abmelden (für Account-Wechsel)
                </a>
            </div>
        </section>
    );
}