import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { authService, type UserRole } from "../service/authService";
import "../styles/pages/AuthCallbackPage.css";

type UserResponse = {
    id: string;
    name: string;
    email: string;
    role: "SELLER" | "CUSTOMER" | "ADMIN";
};

export default function AuthCallbackPage() {
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const finishAuthentication = async () => {
            const selectedRole = localStorage.getItem("selectedRole") as UserRole | null;

            try {
                let user: UserResponse;

                try {
                    user = await authService.getMe();
                    localStorage.removeItem("selectedRole");
                } catch (err) {
                    if (axios.isAxiosError(err) && err.response?.status === 404) {
                        if (!selectedRole) {
                            setError("Keine Rolle gefunden. Bitte registriere dich erneut.");
                            return;
                        }

                        try {
                            user = await authService.register(selectedRole);
                            localStorage.removeItem("selectedRole");
                        } catch {
                            setError("Registrierung fehlgeschlagen.");
                            return;
                        }
                    } else if (axios.isAxiosError(err) && err.response?.status === 401) {
                        setError("Du bist nicht eingeloggt.");
                        return;
                    } else {
                        setError("Ein unerwarteter Fehler ist aufgetreten.");
                        return;
                    }
                }

                if (user.role === "SELLER") {
                    navigate("/seller/onboarding", { replace: true });
                    return;
                }

                if (user.role === "ADMIN") {
                    navigate("/", { replace: true });
                    return;
                }

                navigate("/", { replace: true });
            } finally {
                setLoading(false);
            }
        };

        finishAuthentication();
    }, [navigate]);

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

    return null;
}