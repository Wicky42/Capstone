import type { FC } from "react";
import "./ProductCard.css";
import "./ProductCardSkeleton.css";

const ProductCardSkeleton: FC = () => (
    <article className="product-card product-card--skeleton">
        <div className="product-card__image skeleton-block" />
        <div className="product-card__content">
            <div className="skeleton-block skeleton-chip" />
            <div className="skeleton-block skeleton-title" />
            <div className="skeleton-block skeleton-text" />
            <div className="skeleton-block skeleton-text skeleton-text--short" />
            <div className="skeleton-block skeleton-price" />
        </div>
    </article>
);

export default ProductCardSkeleton;

