import { useNavigate } from "react-router-dom";
import type { Product } from "../../types/product";
import ProductStatusBadge from "../product/ProductStatusBadge";
import "./SellerProductTable.css";

type Props = {
    products: Product[];
    onPublish: (product: Product) => void;
    onDeactivate: (product: Product) => void;
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

function formatPrice(price: number): string {
    return price.toLocaleString("de-DE", { style: "currency", currency: "EUR" });
}

export default function SellerProductTable({
    products,
    onPublish,
    onDeactivate,
    loadingId,
}: Props) {
    const navigate = useNavigate();

    return (
        <div className="spt__wrapper">
            <table className="spt">
                <thead>
                    <tr>
                        <th className="spt__th spt__th--img" aria-label="Bild" />
                        <th className="spt__th">Name</th>
                        <th className="spt__th spt__th--hide-sm">Kategorie</th>
                        <th className="spt__th spt__th--num">Preis</th>
                        <th className="spt__th spt__th--num spt__th--hide-sm">Bestand</th>
                        <th className="spt__th">Status</th>
                        <th className="spt__th spt__th--hide-md">Herstellung</th>
                        <th className="spt__th spt__th--hide-md">MHD</th>
                        <th className="spt__th spt__th--actions">Aktionen</th>
                    </tr>
                </thead>
                <tbody>
                    {products.map((product) => {
                        const isBusy = loadingId === product.id;
                        return (
                            <tr key={product.id} className="spt__row">
                                {/* Bild – Seller-Endpunkt, da öffentlicher Endpunkt nur ACTIVE liefert */}
                                <td className="spt__td spt__td--img">
                                    {product.imageUrl ? (
                                        <img
                                            className="spt__img"
                                            src={`/api/seller/products/${product.id}/image`}
                                            alt={product.name}
                                        />
                                    ) : (
                                        <div className="spt__img-placeholder" aria-hidden />
                                    )}
                                </td>

                                {/* Name */}
                                <td className="spt__td spt__td--name">
                                    <span className="spt__product-name">{product.name}</span>
                                </td>

                                {/* Kategorie */}
                                <td className="spt__td spt__th--hide-sm">
                                    <span className="spt__category">{product.category}</span>
                                </td>

                                {/* Preis */}
                                <td className="spt__td spt__td--num">
                                    {formatPrice(product.price)}
                                </td>

                                {/* Bestand */}
                                <td className="spt__td spt__td--num spt__th--hide-sm">
                                    <span
                                        className={`spt__stock${product.stockQuantity === 0 ? " spt__stock--empty" : ""}`}
                                    >
                                        {product.stockQuantity}
                                    </span>
                                </td>

                                {/* Status */}
                                <td className="spt__td">
                                    <ProductStatusBadge status={product.status} />
                                </td>

                                {/* Produktionsdatum */}
                                <td className="spt__td spt__th--hide-md">
                                    {formatDate(product.productionDate)}
                                </td>

                                {/* MHD */}
                                <td className="spt__td spt__th--hide-md">
                                    {formatDate(product.bestBeforeDate)}
                                </td>

                                {/* Aktionen */}
                                <td className="spt__td spt__td--actions">
                                    <button
                                        className="spt__action spt__action--edit"
                                        onClick={() =>
                                            navigate(`/seller/products/${product.id}/edit`)
                                        }
                                        disabled={isBusy}
                                        title="Bearbeiten"
                                    >
                                        Bearbeiten
                                    </button>

                                    {product.status !== "ACTIVE" && (
                                        <button
                                            className="spt__action spt__action--publish"
                                            onClick={() => onPublish(product)}
                                            disabled={isBusy}
                                            title="Veröffentlichen"
                                        >
                                            {isBusy ? "…" : "Veröffentlichen"}
                                        </button>
                                    )}

                                    {product.status === "ACTIVE" && (
                                        <button
                                            className="spt__action spt__action--deactivate"
                                            onClick={() => onDeactivate(product)}
                                            disabled={isBusy}
                                            title="Deaktivieren"
                                        >
                                            {isBusy ? "…" : "Deaktivieren"}
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


