import type { OnboardingStatus } from "../../types/Onboarding";
import "../../styles/components/seller/OnboardingStatusCard.css";

type Props = {
    status: OnboardingStatus;
};

export default function OnboardingStatusCard({ status }: Props) {
    return (
        <section className="onboarding-status-card">
            <h2 className="onboarding-status-card__title">Dein Onboarding-Status</h2>

            <div className="onboarding-status-card__meta">
                <p><strong>Aktueller Schritt:</strong> {status.currentStep}</p>
                <p><strong>Nächster Schritt:</strong> {status.nextStep}</p>
            </div>

            <p className="onboarding-status-card__message">{status.message}</p>

            <ul className="onboarding-status-card__list">
                <li className={status.shopCreated ? "done" : ""}>
                    Shop erstellt
                </li>
                <li className={status.shopDataComplete ? "done" : ""}>
                    Shopdaten vollständig
                </li>
                <li className={status.firstProductCreated ? "done" : ""}>
                    Erstes Produkt erstellt
                </li>
                <li className={status.onboardingCompleted ? "done" : ""}>
                    Onboarding abgeschlossen
                </li>
            </ul>
        </section>
    );
}