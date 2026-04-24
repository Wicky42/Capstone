import type { FC } from "react";
import { useEffect, useState } from "react";
import "../styles/Header.css";
import { Link, useNavigate } from "react-router-dom";
import { authService } from "../service/authService";

const Header: FC = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isCheckingAuth, setIsCheckingAuth] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const checkAuth = async () => {
            // CSRF-Cookie holen – unabhängig vom Auth-Status, Fehler ignorieren
            try {
                await authService.getCsrf();
            } catch {
                // kein CSRF-Cookie → spätere POST-Requests schlagen ggf. fehl, aber
                // das darf den Auth-Check nicht beeinflussen
            }

            // Auth-Status separat prüfen
            try {
                await authService.getMe();
                setIsAuthenticated(true);
            } catch {
                setIsAuthenticated(false);
            } finally {
                setIsCheckingAuth(false);
            }
        };

        void checkAuth();
    }, []);

    const handleLogin = () => {
        authService.startGithubLogin();
    };

    const handleLogout = async () => {
        // State sofort setzen → Button wechselt direkt zu "Login"
        setIsAuthenticated(false);
        try {
            await authService.logout();
        } catch (error) {
            console.error("Logout failed", error);
            // Selbst wenn der POST fehlschlägt: Session ist ggf. bereits ungültig
        }
        navigate("/logout");
    };

    return (
        <header className="header">
            <div className="header__brand">
                {/* Add Logo here */}
            </div>

            <nav className="header__nav">
                {!isCheckingAuth && (
                    <>
                        <button
                            type="button"
                            className="header__link header__button"
                            onClick={isAuthenticated ? handleLogout : handleLogin}
                        >
                            {isAuthenticated ? "Logout" : "Login"}
                        </button>

                        {!isAuthenticated ? <Link to="/register">Sign in</Link> : <></>}
                    </>
                )}
            </nav>
        </header>
    );
};

export default Header;