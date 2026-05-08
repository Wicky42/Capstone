import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import ProductForm from "../../components/product/ProductForm";
import { productService } from "../../services/productService";
import type { Product, UpdateProductPayload } from "../../types/product";
import { getApiErrorMessage } from "../../utils/getApiErrorMessages";
import "./EditProductPage.css";

export default function EditProductPage() {
    const { productId } = useParams<{ productId: string }>();
    const navigate = useNavigate();

    const [product, setProduct] = useState<Product | null>(null);
    const [isLoadingProduct, setIsLoadingProduct] = useState(true);
    const [loadError, setLoadError] = useState<string | null>(null);

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);

    useEffect(() => {
        async function loadProduct() {
            if (!productId) return;
            try {
                setIsLoadingProduct(true);
                setLoadError(null);
                const data = await productService.getSellerProductById(productId);
                setProduct(data);
            } catch {
                setLoadError("Das Produkt konnte nicht geladen werden.");
            } finally {
                setIsLoadingProduct(false);
            }
        }

        loadProduct();
    }, [productId]);

    async function handleSubmit(payload: UpdateProductPayload, imageFile?: File | null) {
        if (!productId) return;
        try {
            setIsSubmitting(true);
            setSubmitError(null);

            await productService.updateProduct(productId, payload);

            if (imageFile) {
                await productService.uploadProductImage(productId, imageFile);
            }

            navigate("/seller/products");
        } catch (error) {
            setSubmitError(getApiErrorMessage(error));
        } finally {
            setIsSubmitting(false);
        }
    }

    if (isLoadingProduct) {
        return <div className="edit-product-page__state">Produkt wird geladen …</div>;
    }

    if (loadError || !product) {
        return (
            <div className="edit-product-page__state edit-product-page__state--error">
                {loadError ?? "Produkt nicht gefunden."}
            </div>
        );
    }

    return (
        <div className="edit-product-page">
            <header className="edit-product-page__header">
                <h1 className="edit-product-page__title">Produkt bearbeiten</h1>
                <p className="edit-product-page__subtitle">
                    Aktualisiere die Produktdaten und speichere deine Änderungen.
                </p>
            </header>

            <div className="edit-product-page__panel">
                <ProductForm
                    mode="edit"
                    initialProduct={product}
                    isLoading={isSubmitting}
                    errorMessage={submitError}
                    submitLabel="Änderungen speichern"
                    onSubmit={handleSubmit}
                />
            </div>
        </div>
    );
}