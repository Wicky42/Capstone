import type { FC } from "react";
import { Link } from "react-router-dom";
import type { Shop } from "../../types/shop";
import "./ShopCard.css";

type Props = {
    shop: Shop;
};

const ShopCard: FC<Props> = ({ shop }) => (
    <Link to={`/shops/${shop.slug}`} className="shop-card">
        <div className="shop-card__logo">
            {shop.logoUrl ? (
                <img src={shop.logoUrl} alt={`${shop.name} Logo`} className="shop-card__logo-img" />
            ) : (
                <span className="shop-card__logo-placeholder" aria-hidden="true">
                    {shop.name.charAt(0).toUpperCase()}
                </span>
            )}
        </div>
        <div className="shop-card__content">
            <h3 className="shop-card__name">{shop.name}</h3>
            <p className="shop-card__description">{shop.description}</p>
            <span className="shop-card__cta">Zum Shop →</span>
        </div>
    </Link>
);

export default ShopCard;

