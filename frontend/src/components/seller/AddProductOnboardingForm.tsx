import { useState } from "react";
import ProductForm from "../product/ProductForm";
import { productService } from "../../services/productService";
import type { CreateProductPayload } from "../../types/product";
import { getApiErrorMessage } from "../../utils/getApiErrorMessages";

type AddProductOnboardingFormProps = {
    onSuccess: () => Promise<void> | void;
};

export default function AddProductOnboardingForm({
                                                     onSuccess,
                                                 }: AddProductOnboardingFormProps) {
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

            await onSuccess();
        } catch (error) {
            setErrorMessage(getApiErrorMessage(error));
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <section className="seller-onboarding-card">
            <h2>Füge dein erstes Produkt hinzu</h2>
            <p>
                Lege dein erstes Produkt an, damit dein Shop im Onboarding
                abgeschlossen werden kann.
            </p>

            <ProductForm
                mode="create"
                isLoading={isLoading}
                errorMessage={errorMessage}
                submitLabel="Erstes Produkt hinzufügen"
                onSubmit={handleSubmit}
            />
        </section>
    );
}