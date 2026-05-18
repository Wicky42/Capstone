import type { FC } from "react";
import { useEffect, useState } from "react";
import "./Header.css";
import { Link, useNavigate } from "react-router-dom";
import { authService } from "../../services/authService";
import { useCartContext } from "../../context/CartContext";
import logo from "../../assets/nekosto-logo-min.png";


const Header: FC = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isCheckingAuth, setIsCheckingAuth] = useState(true);
    const [userRole, setUserRole] = useState<"SELLER" | "CUSTOMER" | "ADMIN" | null>(null);
    const navigate = useNavigate();
    const { totalItems, clearCart } = useCartContext();


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

    // 1. clearCart aus dem CartContext holen

// 2. handleLogout erweitern
    const handleLogout = async () => {
        setIsAuthenticated(false);
        setUserRole(null);
        clearCart(); // Warenkorb leeren
        localStorage.removeItem("selectedRole"); // Auth-Daten löschen
        try {
            await authService.logout();
        } catch (error) {
            console.error("Logout failed", error);
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
                <Link to="/cart" className="header__cart" aria-label="Warenkorb">
                    <svg className="header__cart-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
                        <line x1="3" y1="6" x2="21" y2="6" />
                        <path d="M16 10a4 4 0 0 1-8 0" />
                    </svg>
                    {totalItems > 0 && (
                        <span className="header__cart-badge">{totalItems > 99 ? "99+" : totalItems}</span>
                    )}
                </Link>
                {!isCheckingAuth && (
                    <>
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