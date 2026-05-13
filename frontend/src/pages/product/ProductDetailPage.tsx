import { type FC, useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import type { Product } from "../../types/product";
import type { Shop } from "../../types/shop";
import { storefrontService } from "../../services/storefrontService";
import { useCartContext } from "../../context/CartContext";
import "./ProductDetailPage.css";
import { NumberField, Input, Group, Button } from "react-aria-components";


const ProductDetailPage: FC = () => {
    const { productId } = useParams<{ productId: string }>();
    const [product, setProduct] = useState<Product | null>(null);
    const [shop, setShop] = useState<Shop | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [quantity, setQuantity] = useState(1);
    const [added, setAdded] = useState(false);

    const { addItem } = useCartContext();

    useEffect(() => {
        if (!productId) return;

        const load = async () => {
            setLoading(true);
            try {
                const p = await storefrontService.getProductById(productId);
                setProduct(p);
                try {
                    const s = await storefrontService.getShopById(p.shopId);
                    setShop(s);
                } catch {
                    // Shop optional
                }
            } catch {
                setError("Produkt nicht gefunden.");
            } finally {
                setLoading(false);
            }
        };

        void load();
    }, [productId]);

    const handleAddToCart = () => {
        if (!product) return;
        addItem(
            {
                productId: product.id,
                productName: product.name,
                unitPrice: product.price,
                maxQuantity: product.stockQuantity,
                titleImage: product.imageUrl ?? null,
                shopId: product.shopId,
                sellerId: product.sellerId,
            },
            quantity
        );
        setAdded(true);
        setTimeout(() => setAdded(false), 1500);
    };

    if (loading) return <div className="container product-detail__loading">Laden…</div>;
    if (error || !product) return (
        <div className="container product-detail__error">
            <p>{error ?? "Produkt nicht gefunden."}</p>
            <Link to="/" className="button-secondary">← Zurück zum Marktplatz</Link>
        </div>
    );

    const imageUrl = storefrontService.getProductImageUrl(product.id);

    return (
        <div className="product-detail page">
            <div className="container">
                <nav className="product-detail__breadcrumb">
                    <Link to="/">Marktplatz</Link>
                    {shop && <><span> / </span><Link to={`/shops/${shop.slug}`}>{shop.name}</Link></>}
                    <span> / {product.name}</span>
                </nav>

                <div className="product-detail__layout">
                    <div className="product-detail__image-wrap">
                        {product.imageUrl ? (
                            <img
                                src={imageUrl}
                                alt={product.name}
                                className="product-detail__image"
                            />
                        ) : (
                            <div className="product-detail__image-placeholder" />
                        )}
                    </div>

                    <div className="product-detail__info">
                        {product.category && (
                            <span className="chip">{product.category}</span>
                        )}
                        <h1 className="product-detail__title">{product.name}</h1>
                        <strong className="product-detail__price">
                            {product.price.toLocaleString("de-DE", { style: "currency", currency: "EUR" })}
                        </strong>
                        <p className="product-detail__description">{product.description}</p>

                        <dl className="product-detail__meta">
                            {product.bestBeforeDate && (
                                <>
                                    <dt>Mindesthaltbar bis</dt>
                                    <dd>{new Date(product.bestBeforeDate).toLocaleDateString("de-DE")}</dd>
                                </>
                            )}
                            {product.productionDate && (
                                <>
                                    <dt>Produktionsdatum</dt>
                                    <dd>{new Date(product.productionDate).toLocaleDateString("de-DE")}</dd>
                                </>
                            )}
                            <dt>Lagerbestand</dt>
                            <dd>{product.stockQuantity} Stück</dd>
                        </dl>

                        <NumberField
                            className="product-detail__quantity"
                            minValue={1}
                            maxValue={product.stockQuantity}
                            value={quantity}
                            onChange={setQuantity}
                        >
                            <div className="product-detail__quantity-row">
                                <Group className="product-detail__stepper">
                                    <Button slot="decrement" className="product-detail__stepper-btn">−</Button>
                                    <Input className="product-detail__stepper-input" />
                                    <Button slot="increment" className="product-detail__stepper-btn">+</Button>
                                </Group>
                                <button
                                    className={`button-primary product-detail__add-btn${added ? " product-detail__add-btn--added" : ""}`}
                                    onClick={handleAddToCart}
                                    disabled={product.stockQuantity === 0}
                                >
                                    {added ? "✓ Hinzugefügt" : "In den Warenkorb"}
                                </button>
                            </div>
                        </NumberField>


                        {shop && (
                            <Link to={`/shops/${shop.slug}`} className="product-detail__shop-link button-secondary">
                                Im Shop von {shop.name} entdecken →
                            </Link>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProductDetailPage;

