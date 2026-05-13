import type { PendingShopVerification } from "../../types/pendingShopVerification";
import type { ShopStatus } from "../../types/shopStatus";
import "./PendingVerificationTable.css";

type Props = {
    shops: PendingShopVerification[];
    onActivate: (shopId: string) => void;
    onDeactivate: (shopId: string) => void;
    onReject: (shopId: string) => void;
    loadingId: string | null;
};

function formatDate(dateStr: string | null | undefined): string {
    if (!dateStr) return "–";
    return new Date(dateStr).toLocaleDateString("de-DE", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
    });
}

function truncate(text: string, max: number): string {
    if (!text) return "–";
    return text.length > max ? text.slice(0, max) + " …" : text;
}

function ShopStatusBadge({ status }: { status: ShopStatus }) {
    const map: Record<ShopStatus, { label: string; className: string }> = {
        DRAFT: { label: "Entwurf", className: "pvt__badge pvt__badge--draft" },
        ACTIVE: { label: "Aktiv", className: "pvt__badge pvt__badge--active" },
        INACTIVE: { label: "Inaktiv", className: "pvt__badge pvt__badge--inactive" },
        REJECTED: { label: "Abgelehnt", className: "pvt__badge pvt__badge--rejected" },
    };
    const { label, className } = map[status] ?? { label: status, className: "pvt__badge" };
    return <span className={className}>{label}</span>;
}

export default function PendingVerificationTable({
    shops,
    onActivate,
    onDeactivate,
    onReject,
    loadingId,
}: Props) {
    return (
        <div className="pvt__wrapper">
            <table className="pvt">
                <thead>
                    <tr>
                        <th className="pvt__th">Shopname</th>
                        <th className="pvt__th pvt__th--hide-sm">Beschreibung</th>
                        <th className="pvt__th">Seller</th>
                        <th className="pvt__th pvt__th--hide-sm">E-Mail</th>
                        <th className="pvt__th pvt__th--num pvt__th--hide-md">Produkte</th>
                        <th className="pvt__th">Status</th>
                        <th className="pvt__th pvt__th--hide-md">Erstellt am</th>
                        <th className="pvt__th pvt__th--hide-md">Aktualisiert am</th>
                        <th className="pvt__th pvt__th--actions">Aktionen</th>
                    </tr>
                </thead>
                <tbody>
                    {shops.map((shop) => {
                        const isBusy = loadingId === shop.shopId;
                        const canActivate = shop.shopStatus !== "ACTIVE";
                        const canDeactivate = shop.shopStatus === "ACTIVE";
                        const canReject = shop.shopStatus !== "REJECTED";

                        return (
                            <tr key={shop.shopId} className="pvt__row">
                                <td className="pvt__td pvt__td--name" data-label="Shopname">
                                    <span className="pvt__shop-name">{shop.shopName}</span>
                                </td>

                                <td className="pvt__td pvt__th--hide-sm" data-label="Beschreibung">
                                    <span className="pvt__description" title={shop.shopDescription}>
                                        {truncate(shop.shopDescription, 80)}
                                    </span>
                                </td>

                                <td className="pvt__td" data-label="Seller">
                                    {shop.sellerName}
                                </td>

                                <td className="pvt__td pvt__th--hide-sm" data-label="E-Mail">
                                    <a className="pvt__email-link" href={`mailto:${shop.sellerEmail}`}>
                                        {shop.sellerEmail}
                                    </a>
                                </td>

                                <td className="pvt__td pvt__td--num pvt__th--hide-md" data-label="Produkte">
                                    {shop.productCount}
                                </td>

                                <td className="pvt__td" data-label="Status">
                                    <ShopStatusBadge status={shop.shopStatus} />
                                </td>

                                <td className="pvt__td pvt__th--hide-md" data-label="Erstellt am">
                                    {formatDate(shop.createdAt)}
                                </td>

                                <td className="pvt__td pvt__th--hide-md" data-label="Aktualisiert am">
                                    {formatDate(shop.updatedAt)}
                                </td>

                                <td className="pvt__td pvt__td--actions" data-label="Aktionen">
                                    {canActivate && (
                                        <button
                                            className="pvt__action pvt__action--activate"
                                            onClick={() => onActivate(shop.shopId)}
                                            disabled={isBusy}
                                            title="Shop aktivieren"
                                        >
                                            {isBusy ? "…" : "Aktivieren"}
                                        </button>
                                    )}

                                    {canDeactivate && (
                                        <button
                                            className="pvt__action pvt__action--deactivate"
                                            onClick={() => onDeactivate(shop.shopId)}
                                            disabled={isBusy}
                                            title="Shop deaktivieren"
                                        >
                                            {isBusy ? "…" : "Deaktivieren"}
                                        </button>
                                    )}

                                    {canReject && (
                                        <button
                                            className="pvt__action pvt__action--reject"
                                            onClick={() => onReject(shop.shopId)}
                                            disabled={isBusy}
                                            title="Shop ablehnen"
                                        >
                                            {isBusy ? "…" : "Ablehnen"}
                                        </button>
                                    )}
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}

