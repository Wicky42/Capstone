import type { FC } from "react";
import "./InformationBanner.css";
import ehrlichNatuerlich from "../../assets/side-info-ehrlich-natuerlich.png";
import handverlesen from "../../assets/side-info-handverlesen.png";
import kleineManufakturen from "../../assets/side-info-kleine-manufacturen.png";
import regional from "../../assets/side-info-regional.png";

const InformationBanner: FC = () => {
    return (
        <section className="info-banner">
            <div className="info-banner__item">
                <img src={ehrlichNatuerlich} alt="Ehrlich & Natürlich – Ohne Schnickschnack" />
            </div>
            <div className="info-banner__item">
                <img src={handverlesen} alt="Handverlesen – Mit Sorgfalt ausgewählt" />
            </div>
            <div className="info-banner__item">
                <img src={kleineManufakturen} alt="Kleine Manufakturen – Echte Menschen, echte Produkte" />
            </div>
            <div className="info-banner__item">
                <img src={regional} alt="Regional – Von hier & mit Herz" />
            </div>
        </section>
    );
};

export default InformationBanner;

