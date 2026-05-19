import type { FC } from "react";
import { useEffect, useRef, useState } from "react";
import "./Header.css";
import { Link, useNavigate } from "react-router-dom";
import { authService } from "../../services/authService";
import { useCartContext } from "../../context/CartContext";
import logo from "../../assets/nekosto-logo-min.png";

// ── Role-based navigation config ──────────────────────────────────────────────
// To add or reorder items: edit the arrays below. No component code changes needed.

type Role = "SELLER" | "CUSTOMER" | "ADMIN";
type NavItem = { label: string; to: string };

const ROLE_NAV: Record<Role, NavItem[]> = {
    CUSTOMER: [
        { label: "Meine Bestellungen", to: "/account/orders" },
        { label: "Meine Kontodaten",   to: "/account/profile" },
    ],
    SELLER: [
        { label: "Bestellungen", to: "/seller/orders" },
        { label: "Dashboard",    to: "/seller/dashboard" },
    ],
    ADMIN: [
        { label: "Shop Bestätigungen", to: "/admin" },
        { label: "Alle Bestellungen",  to: "/admin/orders" },
    ],
};

// ── Component ──────────────────────────────────────────────────────────────────

const Header: FC = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isCheckingAuth, setIsCheckingAuth]   = useState(true);
    const [userRole, setUserRole]               = useState<Role | null>(null);
    const [isAccountOpen, setIsAccountOpen]     = useState(false);
    const accountRef = useRef<HTMLDivElement>(null);
    const navigate = useNavigate();
    const { totalItems, clearCart } = useCartContext();

    useEffect(() => {
        const checkAuth = async () => {
            try { await authService.getCsrf(); } catch { /* ignore */ }
            try {
                const user = await authService.getMe();
                setIsAuthenticated(true);
                setUserRole(user.role as Role);
            } catch {
                setIsAuthenticated(false);
                setUserRole(null);
            } finally {
                setIsCheckingAuth(false);
            }
        };
        void checkAuth();
    }, []);

    // Close dropdown on outside click
    useEffect(() => {
        if (!isAccountOpen) return;
        const handler = (e: MouseEvent) => {
            if (accountRef.current && !accountRef.current.contains(e.target as Node)) {
                setIsAccountOpen(false);
            }
        };
        document.addEventListener("mousedown", handler);
        return () => document.removeEventListener("mousedown", handler);
    }, [isAccountOpen]);

    const handleLogout = async () => {
        setIsAuthenticated(false);
        setUserRole(null);
        setIsAccountOpen(false);
        clearCart();
        localStorage.removeItem("selectedRole");
        try { await authService.logout(); } catch { /* ignore */ }
        navigate("/logout");
    };

    const navItems = userRole ? ROLE_NAV[userRole] : [];

    return (
        <header className="header">
            <div className="header__brand">
                <Link to="/" className="header__brand">
                    <img src={logo} alt="Nekosto Logo" />
                </Link>
            </div>

            <nav className="header__nav">
                {/* Cart icon */}
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
                        {isAuthenticated ? (
                            /* Account dropdown */
                            <div className="header__account" ref={accountRef}>
                                <button
                                    type="button"
                                    className="header__account-trigger"
                                    aria-label="Konto"
                                    aria-expanded={isAccountOpen}
                                    aria-haspopup="menu"
                                    onClick={() => setIsAccountOpen((prev) => !prev)}
                                >
                                    <svg className="header__account-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                                        <circle cx="12" cy="8" r="4" />
                                        <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
                                    </svg>
                                </button>

                                {isAccountOpen && (
                                    <div className="header__account-dropdown" role="menu">
                                        {navItems.map((item) => (
                                            <Link
                                                key={item.to}
                                                to={item.to}
                                                className="header__account-item"
                                                role="menuitem"
                                                onClick={() => setIsAccountOpen(false)}
                                            >
                                                {item.label}
                                            </Link>
                                        ))}
                                        <div className="header__account-divider" role="separator" />
                                        <button
                                            type="button"
                                            className="header__account-item header__account-item--logout"
                                            role="menuitem"
                                            onClick={handleLogout}
                                        >
                                            Abmelden
                                        </button>
                                    </div>
                                )}
                            </div>
                        ) : (
                            /* Guest actions */
                            <>
                                <button
                                    type="button"
                                    className="header__link header__button"
                                    onClick={() => authService.startGithubLogin()}
                                >
                                    Login
                                </button>
                                <Link to="/register" className="button-primary" style={{ textDecoration: "none" }}>
                                    Registrieren
                                </Link>
                            </>
                        )}
                    </>
                )}
            </nav>
        </header>
    );
};

export default Header;
