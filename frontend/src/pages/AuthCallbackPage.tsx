import { useEffect, useState } from "react";
import axios from "axios";
import { authService, type UserRole } from "../service/authService";
import "../styles/pages/AuthCallbackPage.css"

type UserResponse = {
    id: string;
    name: string;
    email: string;
    role: "SELLER" | "CUSTOMER" | "ADMIN";
};

export default function AuthCallbackPage() {
    const [user, setUser] = useState<UserResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const finishAuthentication = async () => {
            const selectedRole = localStorage.getItem("selectedRole") as UserRole | null;

            try {
                const existingUser = await authService.getMe();
                setUser(existingUser);
                localStorage.removeItem("selectedRole");
                return;
            } catch (err) {
                if (axios.isAxiosError(err) && err.response?.status === 404) {
                    if (!selectedRole) {
                        setError("Keine Rolle gefunden. Bitte registriere dich erneut.");
                        return;
                    }

                    try {
                        const registeredUser = await authService.register(selectedRole);
                        setUser(registeredUser);
                        localStorage.removeItem("selectedRole");
                        return;
                    } catch {
                        setError("Registrierung fehlgeschlagen.");
                        return;
                    }
                }

                if (axios.isAxiosError(err) && err.response?.status === 401) {
                    setError("Du bist nicht eingeloggt.");
                    return;
                }

                setError("Ein unerwarteter Fehler ist aufgetreten.");
            } finally {
                setLoading(false);
            }
        };

        finishAuthentication();
    }, []);

    if (loading) {
        return (
            <section className="auth-callback-page">
                <div className="auth-callback-card">
                    <h2>Authentifizierung läuft...</h2>
                    <p>Dein Login wird gerade abgeschlossen.</p>
                </div>
            </section>
        );
    }

    if (error) {
        return (
            <section className="auth-callback-page">
                <div className="auth-callback-card">
                    <h2>Fehler bei der Anmeldung</h2>
                    <p>{error}</p>
                </div>
            </section>
        );
    }

    return (
        <section className="auth-callback-page">
            <div className="auth-callback-card">
                <h1>Willkommen, {user?.name}!</h1>
                <p>Du bist in der Rolle <strong>{user?.role}</strong>.</p>
                <p className="auth-callback-email">{user?.email}</p>
            </div>
        </section>
    );
}