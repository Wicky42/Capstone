import React from "react";
import "./Footer.css";

const Footer: React.FC = () => (
    <footer className="footer">
        <div className="footer__inner">
            <span className="footer__brand">
                {/* Logo hier einfügen */}
                Nekosto
            </span>
            <span className="footer__copy">
                &copy; {new Date().getFullYear()} Nekosto - Nah. Köstlich. Alle Rechte vorbehalten.
            </span>
        </div>
    </footer>
);

export default Footer;
