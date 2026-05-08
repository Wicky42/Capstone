import type { OnboardingStatus } from "../../types/onboarding";
import "./OnboardingStatusCard.css";

type Props = {
    status: OnboardingStatus;
};

export default function OnboardingStatusCard({ status }: Props) {
    return (
        <section className="onboarding-status-card">
            <h2 className="onboarding-status-card__title">Fortschritt</h2>

            <p className="onboarding-status-card__message">{status.message}</p>

            <ul className="onboarding-status-card__list">
                <li className={status.shopCreated ? "done" : ""}> Shop erstellt</li>
                <li className={status.shopDataCompleted ? "done" : ""}> Rechtliche Daten vollständig</li>
                <li className={status.firstProductCreated ? "done" : ""}> Erstes Produkt erstellt</li>
                <li className={status.onBoardingCompleted ? "done" : ""}> Onboarding abgeschlossen</li>
            </ul>
        </section>
    );
}