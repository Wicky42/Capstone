import type { FC } from "react";
import { useEffect, useState } from "react";
import "./Header.css";
import { Link, useNavigate } from "react-router-dom";
import { authService } from "../../services/authService";
import logo from "../../assets/nekosto-logo-min.png";


const Header: FC = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isCheckingAuth, setIsCheckingAuth] = useState(true);
    const [userRole, setUserRole] = useState<"SELLER" | "CUSTOMER" | "ADMIN" | null>(null);
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
                const user = await authService.getMe();
                setIsAuthenticated(true);
                setUserRole(user.role)
            } catch {
                setIsAuthenticated(false);
                setUserRole(null);
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
                {/* Logo hier einfügen */}
                <Link to="/" className="header__brand">
                    <img src={logo} alt="Nekosto Logo"/>
                </Link>
            </div>

            <nav className="header__nav">
                {!isCheckingAuth && (
                    <>
                        {/* TODO 1 : Check if current user is Seller -if yes : show button "Zum Dashboard" and navigate to "/seller/dashboard"*/}
                        {isAuthenticated && userRole === "SELLER" && (
                            <button
                                type="button"
                                className="header__link header__button"
                                onClick={() => navigate("/seller/dashboard")}
                            >
                                Zum Dashboard
                            </button>
                        )}

                        <button
                            type="button"
                            className="header__link header__button"
                            onClick={isAuthenticated ? handleLogout : handleLogin}
                        >
                            {isAuthenticated ? "Logout" : "Login"}
                        </button>

                        {!isAuthenticated && (
                            <Link to="/register" className="button-primary" style={{ textDecoration: 'none' }}>
                                Registrieren
                            </Link>
                        )}
                    </>
                )}
            </nav>
        </header>
    );
};

export default Header;