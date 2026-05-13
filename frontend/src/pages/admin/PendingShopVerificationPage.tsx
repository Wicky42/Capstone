import { useCallback, useEffect, useState, useTransition } from "react";
import { adminShopService } from "../../services/adminShopService";
import type { PendingShopVerification } from "../../types/pendingShopVerification";
import PendingVerificationTable from "../../components/admin/PendingVerificationTable";
import "./PendingShopVerificationPage.css";

export default function PendingShopVerificationPage() {
    const [shops, setShops] = useState<PendingShopVerification[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);
    const [actionError, setActionError] = useState<string | null>(null);
    const [isPending, startTransition] = useTransition();

    const loadShops = useCallback(async () => {
        try {
            setError(null);
            const data = await adminShopService.getPendingShopVerifications();
            setShops(data);
        } catch {
            setError("Shops konnten nicht geladen werden. Bitte versuche es erneut.");
        }
    }, []);

    useEffect(() => {
        startTransition(() => { loadShops(); });
    }, [loadShops]);

    async function handleActivate(shopId: string) {
        try {
            setActionLoadingId(shopId);
            setActionError(null);
            await adminShopService.activateShop(shopId);
            await loadShops();
        } catch {
            setActionError("Shop konnte nicht aktiviert werden.");
        } finally {
            setActionLoadingId(null);
        }
    }

    async function handleDeactivate(shopId: string) {
        try {
            setActionLoadingId(shopId);
            setActionError(null);
            await adminShopService.deactivate(shopId);
            await loadShops();
        } catch {
            setActionError("Shop konnte nicht deaktiviert werden.");
        } finally {
            setActionLoadingId(null);
        }
    }

    async function handleReject(shopId: string) {
        try {
            setActionLoadingId(shopId);
            setActionError(null);
            await adminShopService.rejectShop(shopId);
            await loadShops();
        } catch {
            setActionError("Shop konnte nicht abgelehnt werden.");
        } finally {
            setActionLoadingId(null);
        }
    }

    const isLoading = isPending;

    return (
        <div className="pending-page">
            {/* Header */}
            <header className="pending-page__header">
                <div>
                    <h1 className="pending-page__title">Shop-Freigaben</h1>
                    <p className="pending-page__subtitle">
                        Prüfe Shops, deren Onboarding abgeschlossen wurde.
                    </p>
                </div>
                {!isLoading && !error && (
                    <span className="pending-page__count">
                        {shops.length} {shops.length === 1 ? "Shop" : "Shops"} ausstehend
                    </span>
                )}
            </header>

            {/* Aktions-Fehlermeldung */}
            {actionError && (
                <div className="pending-page__action-error" role="alert">
                    {actionError}
                </div>
            )}

            {/* Loading */}
            {isLoading && (
                <div className="pending-page__state">
                    Shops werden geladen …
                </div>
            )}

            {/* Error */}
            {!isLoading && error && (
                <div className="pending-page__state pending-page__state--error" role="alert">
                    {error}
                </div>
            )}

            {/* Empty State */}
            {!isLoading && !error && shops.length === 0 && (
                <div className="pending-page__empty">
                    <div className="pending-page__empty-icon" aria-hidden>🏪</div>
                    <h2 className="pending-page__empty-title">Alles erledigt</h2>
                    <p className="pending-page__empty-text">
                        Aktuell warten keine Shops auf Freigabe.
                    </p>
                </div>
            )}

            {/* Tabelle */}
            {!isLoading && !error && shops.length > 0 && (
                <PendingVerificationTable
                    shops={shops}
                    onActivate={handleActivate}
                    onDeactivate={handleDeactivate}
                    onReject={handleReject}
                    loadingId={actionLoadingId}
                />
            )}
        </div>
    );
}