import type { FC } from "react";
import "../styles/Header.css"
import { Link } from "react-router-dom";


const Header: FC = () => (
    <header className="header">
        <div className="header__brand">
            {/* Addd Logo here */}
        </div>

        <nav className="header__nav">
            {/* Add Links here */}
            <Link to="/login">Login</Link>
            <Link to="/register">Sign in</Link>

        </nav>
    </header>
);

export default Header;