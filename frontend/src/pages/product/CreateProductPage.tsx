import { useState } from "react";
import { useNavigate } from "react-router-dom";
import ProductForm from "../../components/product/ProductForm";
import { productService } from "../../services/productService";
import type { CreateProductPayload } from "../../types/product";
import { getApiErrorMessage } from "../../utils/getApiErrorMessages";
import "./CreateProductPage.css";

export default function CreateProductPage() {
    const navigate = useNavigate();

    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    async function handleSubmit(
        payload: CreateProductPayload,
        imageFile?: File | null
    ) {
        try {
            setIsLoading(true);
            setErrorMessage(null);

            await productService.createProductWithOptionalImage(payload, imageFile);

            navigate("/seller/products");
        } catch (error) {
            setErrorMessage(getApiErrorMessage(error));
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="create-product-page">
            <header className="create-product-page__header">
                <h1 className="create-product-page__title">Neues Produkt anlegen</h1>
                <p className="create-product-page__subtitle">
                    Fülle alle Pflichtfelder aus, um dein Produkt zu veröffentlichen.
                </p>
            </header>

            <div className="create-product-page__panel">
                <ProductForm
                    mode="create"
                    isLoading={isLoading}
                    errorMessage={errorMessage}
                    submitLabel="Produkt erstellen"
                    onSubmit={handleSubmit}
                />
            </div>
        </div>
    );
}