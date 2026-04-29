import { useState } from "react";
import { useNavigate } from "react-router-dom";
import ProductForm from "../../components/product/ProductForm";
import { productService } from "../../services/productService";
import type { CreateProductPayload } from "../../types/product";
import { getApiErrorMessage } from "../../utils/getApiErrorMessages";

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
        <main>
            <h1>Neues Produkt anlegen</h1>

            <ProductForm
                mode="create"
                isLoading={isLoading}
                errorMessage={errorMessage}
                submitLabel="Produkt erstellen"
                onSubmit={handleSubmit}
            />
        </main>
    );
}