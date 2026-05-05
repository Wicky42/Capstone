import React from "react";
import "../styles/Footer.css";

const Footer: React.FC = () => (
    <footer className="footer">
        <div className="footer__inner">
            <span className="footer__brand">
                {/* Logo hier einfügen */}
                Marktplatz
            </span>
            <span className="footer__copy">
                &copy; {new Date().getFullYear()} Nischen-Marktplatz. Alle Rechte vorbehalten.
            </span>
        </div>
    </footer>
);

export default Footer;
