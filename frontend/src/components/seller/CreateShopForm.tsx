import { useState } from "react";
import { createShop } from "../../service/shopService.ts";

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
        <section>
            <h2>Shop erstellen</h2>

            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="shop-name">Shopname</label>
                    <input
                        id="shop-name"
                        type="text"
                        value={name}
                        onChange={(event) => setName(event.target.value)}
                        disabled={isSubmitting}
                        required
                    />
                </div>

                <div>
                    <label htmlFor="shop-description">Beschreibung</label>
                    <textarea
                        id="shop-description"
                        value={description}
                        onChange={(event) => setDescription(event.target.value)}
                        disabled={isSubmitting}
                        required
                        rows={5}
                    />
                </div>

                {error && <p>{error}</p>}

                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? "Wird erstellt ..." : "Shop erstellen"}
                </button>
            </form>
        </section>
    );
}