import {type FormEvent, useMemo, useState } from "react";
import { parseDate } from "@internationalized/date";
import type { DateValue } from "react-aria-components";
import "./ProductForm.css";
import type {
    CreateProductPayload,
    Product,
    ProductStatus,
    UpdateProductPayload,
} from "../../types/product";
import DatePickerField from "../base/date-picker/DatePickerField";

type ProductFormValues = CreateProductPayload | UpdateProductPayload;

type ProductFormProps = {
    mode: "create" | "edit";
    initialProduct?: Product;
    isLoading?: boolean;
    errorMessage?: string | null;
    submitLabel?: string;
    onSubmit: (payload: ProductFormValues, imageFile?: File | null) => Promise<void> | void;
};

type ValidationErrors = {
    name?: string;
    description?: string;
    price?: string;
    category?: string;
    productionDate?: string;
    bestBeforeDate?: string;
    stockQuantity?: string;
    status?: string;
    image?: string;
};

const MAX_IMAGE_SIZE_IN_BYTES = 5 * 1024 * 1024;

const ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];

export default function ProductForm({
                                        mode,
                                        initialProduct,
                                        isLoading = false,
                                        errorMessage,
                                        submitLabel,
                                        onSubmit,
                                    }: ProductFormProps) {
    const [name, setName] = useState(initialProduct?.name ?? "");
    const [description, setDescription] = useState(initialProduct?.description ?? "");
    const [price, setPrice] = useState(
        initialProduct?.price !== undefined ? String(initialProduct.price) : ""
    );
    const [category, setCategory] = useState(initialProduct?.category ?? "");
    const [productionDate, setProductionDate] = useState<DateValue | null>(
        initialProduct?.productionDate ? parseDate(initialProduct.productionDate) : null
    );
    const [bestBeforeDate, setBestBeforeDate] = useState<DateValue | null>(
        initialProduct?.bestBeforeDate ? parseDate(initialProduct.bestBeforeDate) : null
    );
    const [stockQuantity, setStockQuantity] = useState(
        initialProduct?.stockQuantity !== undefined
            ? String(initialProduct.stockQuantity)
            : "0"
    );
    const [status, setStatus] = useState<ProductStatus>(
        initialProduct?.status ?? "DRAFT"
    );

    const [imageFile, setImageFile] = useState<File | null>(null);
    const [imagePreviewUrl, setImagePreviewUrl] = useState<string | null>(
        initialProduct?.imageUrl ?? null
    );

    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});
    const [isDragging, setIsDragging] = useState(false);

    const effectiveSubmitLabel = useMemo(() => {
        if (submitLabel) return submitLabel;
        return mode === "create" ? "Produkt erstellen" : "Produkt speichern";
    }, [mode, submitLabel]);

    function validateImage(file: File): string | null {
        if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
            return "Bitte lade nur JPG, PNG oder WEBP hoch.";
        }

        if (file.size > MAX_IMAGE_SIZE_IN_BYTES) {
            return "Das Bild darf maximal 5 MB groß sein.";
        }

        return null;
    }

    function handleImageFile(file: File) {
        const imageError = validateImage(file);

        if (imageError) {
            setValidationErrors((previous) => ({
                ...previous,
                image: imageError,
            }));
            setImageFile(null);
            return;
        }

        setImageFile(file);
        setImagePreviewUrl(URL.createObjectURL(file));

        setValidationErrors((previous) => ({
            ...previous,
            image: undefined,
        }));
    }

    function validateForm(): ValidationErrors {
        const errors: ValidationErrors = {};

        const numericPrice = Number(price);
        const numericStockQuantity = Number(stockQuantity);

        if (!name.trim()) {
            errors.name = "Name ist erforderlich.";
        }

        if (!description.trim()) {
            errors.description = "Beschreibung ist erforderlich.";
        }

        if (!category.trim()) {
            errors.category = "Kategorie ist erforderlich.";
        }

        if (!price.trim()) {
            errors.price = "Preis ist erforderlich.";
        } else if (Number.isNaN(numericPrice) || numericPrice <= 0) {
            errors.price = "Preis muss größer als 0 sein.";
        }

        if (!stockQuantity.trim()) {
            errors.stockQuantity = "Lagerbestand ist erforderlich.";
        } else if (
            Number.isNaN(numericStockQuantity) ||
            !Number.isInteger(numericStockQuantity) ||
            numericStockQuantity < 0
        ) {
            errors.stockQuantity = "Lagerbestand darf nicht negativ sein.";
        }

        if (productionDate && bestBeforeDate && bestBeforeDate.compare(productionDate) < 0) {
            errors.bestBeforeDate =
                "Das Mindesthaltbarkeitsdatum darf nicht vor dem Produktionsdatum liegen.";
        }

        if (!status) {
            errors.status = "Status ist erforderlich.";
        }

        return errors;
    }

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const errors = validateForm();
        setValidationErrors(errors);

        if (Object.keys(errors).length > 0) {
            return;
        }

        const payload: ProductFormValues = {
            name: name.trim(),
            description: description.trim(),
            price: Number(price),
            category: category.trim(),
            productionDate: productionDate ? productionDate.toString() : null,
            bestBeforeDate: bestBeforeDate ? bestBeforeDate.toString() : null,
            stockQuantity: Number(stockQuantity),
            status,
        };

        await onSubmit(payload, imageFile);
    }

    return (
        <form className="product-form" onSubmit={handleSubmit} noValidate>
            {errorMessage && (
                <div className="product-form__alert" role="alert">
                    {errorMessage}
                </div>
            )}

            <div className="product-form__field">
                <label className="product-form__label" htmlFor="product-name">Name</label>
                <input
                    id="product-name"
                    className="product-form__input"
                    type="text"
                    value={name}
                    onChange={(event) => setName(event.target.value)}
                    disabled={isLoading}
                />
                {validationErrors.name && (
                    <p className="product-form__validation-error">{validationErrors.name}</p>
                )}
            </div>

            <div className="product-form__field">
                <label className="product-form__label" htmlFor="product-description">Beschreibung</label>
                <textarea
                    id="product-description"
                    className="product-form__textarea"
                    value={description}
                    onChange={(event) => setDescription(event.target.value)}
                    disabled={isLoading}
                />
                {validationErrors.description && (
                    <p className="product-form__validation-error">{validationErrors.description}</p>
                )}
            </div>

            <div className="product-form__field">
                <label className="product-form__label" htmlFor="product-price">Preis</label>
                <input
                    id="product-price"
                    className="product-form__input"
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={price}
                    onChange={(event) => setPrice(event.target.value)}
                    disabled={isLoading}
                />
                {validationErrors.price && (
                    <p className="product-form__validation-error">{validationErrors.price}</p>
                )}
            </div>

            <div className="product-form__field">
                <label className="product-form__label" htmlFor="product-category">Kategorie</label>
                <input
                    id="product-category"
                    className="product-form__input"
                    type="text"
                    value={category}
                    onChange={(event) => setCategory(event.target.value)}
                    disabled={isLoading}
                />
                {validationErrors.category && (
                    <p className="product-form__validation-error">{validationErrors.category}</p>
                )}
            </div>

            <DatePickerField
                id="product-production-date"
                label="Herstellungsdatum"
                value={productionDate}
                onChange={setProductionDate}
                isDisabled={isLoading}
                errorMessage={validationErrors.productionDate}
            />

            <DatePickerField
                id="product-best-before-date"
                label="Ablaufdatum (MHD)"
                value={bestBeforeDate}
                onChange={setBestBeforeDate}
                isDisabled={isLoading}
                errorMessage={validationErrors.bestBeforeDate}
            />

            <div className="product-form__field">
                <label className="product-form__label" htmlFor="product-stock-quantity">Lagerbestand</label>
                <input
                    id="product-stock-quantity"
                    className="product-form__input"
                    type="number"
                    min="0"
                    step="1"
                    value={stockQuantity}
                    onChange={(event) => setStockQuantity(event.target.value)}
                    disabled={isLoading}
                />
                {validationErrors.stockQuantity && (
                    <p className="product-form__validation-error">{validationErrors.stockQuantity}</p>
                )}
            </div>

            <div className="product-form__field">
                <label className="product-form__label" htmlFor="product-status">Status</label>
                <select
                    id="product-status"
                    className="product-form__select"
                    value={status}
                    onChange={(event) => setStatus(event.target.value as ProductStatus)}
                    disabled={isLoading}
                >
                    <option value="ACTIVE">Aktiv</option>
                    <option value="INACTIVE">Inaktiv</option>
                    <option value="DRAFT">Entwurf</option>
                </select>
                {validationErrors.status && (
                    <p className="product-form__validation-error">{validationErrors.status}</p>
                )}
            </div>

            <div className="product-form__field">
                <label className="product-form__label" htmlFor="product-image">Produktbild</label>

                <div
                    className={`product-form__drop-zone${isDragging ? " product-form__drop-zone--dragging" : ""}`}
                    onDragOver={(event) => {
                        event.preventDefault();
                        setIsDragging(true);
                    }}
                    onDragLeave={() => setIsDragging(false)}
                    onDrop={(event) => {
                        event.preventDefault();
                        setIsDragging(false);

                        const file = event.dataTransfer.files[0];

                        if (file) {
                            handleImageFile(file);
                        }
                    }}
                >
                    <p className="product-form__drop-zone-hint">Bild hierher ziehen oder auswählen</p>

                    <input
                        id="product-image"
                        className="product-form__file-input"
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        disabled={isLoading}
                        onChange={(event) => {
                            const file = event.target.files?.[0];

                            if (file) {
                                handleImageFile(file);
                            }
                        }}
                    />
                </div>

                {validationErrors.image && (
                    <p className="product-form__validation-error">{validationErrors.image}</p>
                )}

                {imagePreviewUrl && (
                    <div className="product-form__preview">
                        <p className="product-form__preview-label">Vorschau</p>
                        <img
                            className="product-form__preview-image"
                            src={imagePreviewUrl}
                            alt="Produktvorschau"
                        />
                    </div>
                )}
            </div>

            <button className="product-form__submit" type="submit" disabled={isLoading}>
                {isLoading ? "Speichern..." : effectiveSubmitLabel}
            </button>
        </form>
    );
}