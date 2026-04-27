import type { OnboardingStatus } from "../../types/Onboarding";

type Props = {
    status: OnboardingStatus;
};

export default function OnboardingStatusCard({ status }: Props) {
    return (
        <section>
            <h2>Dein Onboarding-Status</h2>

            <p>
                <strong>Aktueller Schritt:</strong> {status.currentStep}
            </p>

            <p>
                <strong>Nächster Schritt:</strong> {status.nextStep}
            </p>

            <p>{status.message}</p>

            <ul>
                <li>Shop erstellt: {status.shopCreated ? "Ja" : "Nein"}</li>
                <li>Shopdaten vollständig: {status.shopDataComplete ? "Ja" : "Nein"}</li>
                <li>Erstes Produkt erstellt: {status.firstProductCreated ? "Ja" : "Nein"}</li>
                <li>Onboarding abgeschlossen: {status.onboardingCompleted ? "Ja" : "Nein"}</li>
            </ul>
        </section>
    );
}