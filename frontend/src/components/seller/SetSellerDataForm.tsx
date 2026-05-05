import { useEffect, useState } from "react";
import { getSellerProfile, updateSellerProfile } from "../../service/sellerService.ts";
import type { SellerData } from "../../types/SellerData";
import "../../styles/components/seller/SetSellerDataForm.css";

type Props = {
    onSuccess: () => Promise<void> | void;
};

const emptyAddress = {
    street: "",
    houseNumber: "",
    postalCode: "",
    city: "",
    country: "Deutschland",
};

const emptySellerData: SellerData = {
    businessName: "",
    taxId: "",
    address: { ...emptyAddress },
    billingAddress: { ...emptyAddress },
};

export default function SetSellerDataForm({ onSuccess }: Props) {
    const [formData, setFormData] = useState<SellerData>(emptySellerData);
    const [isLoading, setIsLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function loadSellerData() {
        try {
            setIsLoading(true);
            setError(null);

            const data = await getSellerProfile();
            setFormData({
                businessName: data.businessName ?? "",
                taxId: data.taxId ?? "",
                address: data.address ?? { ...emptyAddress },
                billingAddress: data.billingAddress ?? { ...emptyAddress },
            });
        } catch {
            setError("Die Seller-Daten konnten nicht geladen werden.");
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        loadSellerData();
    }, []);

    function handleChange(field: keyof SellerData, value: string) {
        setFormData((prev) => ({
            ...prev,
            [field]: value,
        }));
    }

    function handleAddressChange(
        section: "address" | "billingAddress",
        field: "street" | "houseNumber" | "postalCode" | "city" | "country",
        value: string
    ) {
        setFormData((prev) => ({
            ...prev,
            [section]: {
                ...prev[section],
                [field]: value,
            },
        }));
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();

        try {
            setIsSubmitting(true);
            setError(null);

            await updateSellerProfile(formData);
            await onSuccess();
        } catch {
            setError("Die Seller-Daten konnten nicht gespeichert werden.");
        } finally {
            setIsSubmitting(false);
        }
    }

    if (isLoading) {
        return <p className="seller-data-form__loading">Seller-Daten werden geladen …</p>;
    }

    return (
        <section className="seller-data-form">
            <h2 className="seller-data-form__title">Rechtliche Daten vervollständigen</h2>
            <p className="seller-data-form__subtitle">
                Diese Angaben werden für Rechnungen und die Finanzverwaltung benötigt.
            </p>

            <form className="seller-data-form__body" onSubmit={handleSubmit}>
                <div className="seller-data-form__field">
                    <label className="seller-data-form__label" htmlFor="businessName">
                        Firmenname / Gewerbename
                    </label>
                    <input
                        id="businessName"
                        type="text"
                        className="seller-data-form__input"
                        value={formData.businessName}
                        onChange={(e) => handleChange("businessName", e.target.value)}
                        disabled={isSubmitting}
                        required
                    />
                </div>

                <div className="seller-data-form__field">
                    <label className="seller-data-form__label" htmlFor="taxId">
                        Steuer-ID
                    </label>
                    <input
                        id="taxId"
                        type="text"
                        className="seller-data-form__input"
                        value={formData.taxId}
                        onChange={(e) => handleChange("taxId", e.target.value)}
                        disabled={isSubmitting}
                        required
                    />
                </div>

                <fieldset className="seller-data-form__fieldset">
                    <legend className="seller-data-form__legend">Geschäftsadresse</legend>

                    <div className="seller-data-form__address-row">
                        <input
                            type="text"
                            className="seller-data-form__input"
                            placeholder="Straße"
                            value={formData.address.street}
                            onChange={(e) => handleAddressChange("address", "street", e.target.value)}
                            disabled={isSubmitting}
                            required
                        />
                        <input
                            type="text"
                            className="seller-data-form__input seller-data-form__input--short"
                            placeholder="Nr."
                            value={formData.address.houseNumber}
                            onChange={(e) => handleAddressChange("address", "houseNumber", e.target.value)}
                            disabled={isSubmitting}
                            required
                        />
                    </div>

                    <div className="seller-data-form__city-row">
                        <input
                            type="text"
                            className="seller-data-form__input"
                            placeholder="PLZ"
                            value={formData.address.postalCode}
                            onChange={(e) => handleAddressChange("address", "postalCode", e.target.value)}
                            disabled={isSubmitting}
                            required
                        />
                        <input
                            type="text"
                            className="seller-data-form__input"
                            placeholder="Stadt"
                            value={formData.address.city}
                            onChange={(e) => handleAddressChange("address", "city", e.target.value)}
                            disabled={isSubmitting}
                            required
                        />
                    </div>

                    <input
                        type="text"
                        className="seller-data-form__input"
                        placeholder="Land"
                        value={formData.address.country}
                        onChange={(e) => handleAddressChange("address", "country", e.target.value)}
                        disabled={isSubmitting}
                        required
                    />
                </fieldset>

                <fieldset className="seller-data-form__fieldset">
                    <legend className="seller-data-form__legend">Rechnungsadresse</legend>

                    <div className="seller-data-form__address-row">
                        <input
                            type="text"
                            className="seller-data-form__input"
                            placeholder="Straße"
                            value={formData.billingAddress.street}
                            onChange={(e) => handleAddressChange("billingAddress", "street", e.target.value)}
                            disabled={isSubmitting}
                            required
                        />
                        <input
                            type="text"
                            className="seller-data-form__input seller-data-form__input--short"
                            placeholder="Nr."
                            value={formData.billingAddress.houseNumber}
                            onChange={(e) => handleAddressChange("billingAddress", "houseNumber", e.target.value)}
                            disabled={isSubmitting}
                            required
                        />
                    </div>

                    <div className="seller-data-form__city-row">
                        <input
                            type="text"
                            className="seller-data-form__input"
                            placeholder="PLZ"
                            value={formData.billingAddress.postalCode}
                            onChange={(e) => handleAddressChange("billingAddress", "postalCode", e.target.value)}
                            disabled={isSubmitting}
                            required
                        />
                        <input
                            type="text"
                            className="seller-data-form__input"
                            placeholder="Stadt"
                            value={formData.billingAddress.city}
                            onChange={(e) => handleAddressChange("billingAddress", "city", e.target.value)}
                            disabled={isSubmitting}
                            required
                        />
                    </div>

                    <input
                        type="text"
                        className="seller-data-form__input"
                        placeholder="Land"
                        value={formData.billingAddress.country}
                        onChange={(e) => handleAddressChange("billingAddress", "country", e.target.value)}
                        disabled={isSubmitting}
                        required
                    />
                </fieldset>

                {error && <p className="seller-data-form__error">{error}</p>}

                <button
                    type="submit"
                    className="seller-data-form__submit"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? "Wird gespeichert …" : "Daten speichern →"}
                </button>
            </form>
        </section>
    );
}