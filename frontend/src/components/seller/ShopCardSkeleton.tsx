import type { FC } from "react";
import "./ShopCard.css";
import "./ShopCardSkeleton.css";

const ShopCardSkeleton: FC = () => (
    <div className="shop-card shop-card--skeleton">
        <div className="skeleton-block shop-card__logo" />
        <div className="shop-card__content">
            <div className="skeleton-block skeleton-name" />
            <div className="skeleton-block skeleton-desc" />
            <div className="skeleton-block skeleton-desc skeleton-desc--short" />
        </div>
    </div>
);

export default ShopCardSkeleton;

