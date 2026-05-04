import type { ProductStatus } from "../../types/product";
import "./ProductStatusBadge.css";

const STATUS_LABEL: Record<ProductStatus, string> = {
    ACTIVE: "Aktiv",
    DRAFT: "Entwurf",
    INACTIVE: "Inaktiv",
};

type Props = { status: ProductStatus };

export default function ProductStatusBadge({ status }: Props) {
    return (
        <span className={`psb psb--${status.toLowerCase()}`}>
            <span className="psb__dot" aria-hidden />
            {STATUS_LABEL[status]}
        </span>
    );
}

