import type { FC } from "react";
import { Link } from "react-router-dom";
import type { Product } from "../../types/product";
import { storefrontService } from "../../services/storefrontService";
import "./ProductCard.css";

type Props = {
    product: Product;
    shopSlug?: string;
};

const ProductCard: FC<Props> = ({ product, shopSlug }) => {
    const imageUrl = storefrontService.getProductImageUrl(product.id);

    return (
        <article className="product-card">
            <Link to={`/products/${product.id}`} className="product-card__image-link">
                <img
                    className="product-card__image"
                    src={product.imageUrl ? imageUrl : undefined}
                    alt={product.name}
                    onError={(e) => {
                        (e.currentTarget as HTMLImageElement).style.display = "none";
                    }}
                />
                {!product.imageUrl && (
                    <div className="product-card__image-placeholder" aria-hidden="true" />
                )}
            </Link>

            <div className="product-card__content">
                {shopSlug && (
                    <Link to={`/shops/${shopSlug}`} className="product-card__link">
                        Shop entdecken →
                    </Link>
                )}
                {product.category && (
                    <span className="chip product-card__chip">{product.category}</span>
                )}
                <Link to={`/products/${product.id}`} className="product-card__title-link">
                    <h3 className="product-card__title">{product.name}</h3>
                </Link>
                <p className="product-card__description">{product.description}</p>
                <strong className="product-card__price">
                    {product.price.toLocaleString("de-DE", { style: "currency", currency: "EUR" })}
                </strong>
            </div>
        </article>
    );
};

export default ProductCard;

