import { useState } from "react";
import { createShop } from "../../service/shopService.ts";
import "../../styles/components/seller/CreateShopForm.css";

type Props = {
    onSuccess: () => Promise<void> | void;
};

export default function CreateShopForm({ onSuccess }: Props) {
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();

        try {
            setIsSubmitting(true);
            setError(null);

            await createShop({
                name,
                description,
            });

            setName("");
            setDescription("");

            await onSuccess();
        } catch (err) {
            setError("Der Shop konnte nicht erstellt werden.");
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <section className="create-shop-form">
            <h2 className="create-shop-form__title">Shop erstellen</h2>

            <form className="create-shop-form__body" onSubmit={handleSubmit}>
                <div className="create-shop-form__field">
                    <label className="create-shop-form__label" htmlFor="shop-name">
                        Shopname
                    </label>
                    <input
                        id="shop-name"
                        type="text"
                        className="create-shop-form__input"
                        value={name}
                        onChange={(event) => setName(event.target.value)}
                        disabled={isSubmitting}
                        placeholder="z. B. Honigstube"
                        required
                    />
                </div>

                <div className="create-shop-form__field">
                    <label className="create-shop-form__label" htmlFor="shop-description">
                        Beschreibung
                    </label>
                    <textarea
                        id="shop-description"
                        className="create-shop-form__textarea"
                        value={description}
                        onChange={(event) => setDescription(event.target.value)}
                        disabled={isSubmitting}
                        placeholder="Was macht deinen Shop besonders?"
                        required
                        rows={5}
                    />
                </div>

                {error && <p className="create-shop-form__error">{error}</p>}

                <button
                    type="submit"
                    className="create-shop-form__submit"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? "Wird erstellt …" : "Shop erstellen →"}
                </button>
            </form>
        </section>
    );
}