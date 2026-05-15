import type { FC } from "react";
import { useCategories } from "../../hooks/useCategories";
import "./CategoryFilterBar.css";

type Props = {
    selected: string | undefined;
    onChange: (category: string | undefined) => void;
};

const CategoryFilterBar: FC<Props> = ({ selected, onChange }) => {
    const { categories, loading, error } = useCategories();

    return (
        <div className="category-filter-bar" role="group" aria-label="Kategorie filtern">
            <button
                className={`chip category-filter-bar__chip${!selected ? " category-filter-bar__chip--active" : ""}`}
                onClick={() => onChange(undefined)}
                type="button"
            >
                Alle
            </button>
            {!loading && !error && categories.map((cat) => (
                <button
                    key={cat.value}
                    className={`chip category-filter-bar__chip${selected === cat.value ? " category-filter-bar__chip--active" : ""}`}
                    onClick={() => onChange(selected === cat.value ? undefined : cat.value)}
                    type="button"
                >
                    {cat.label}
                </button>
            ))}
        </div>
    );
};

export default CategoryFilterBar;
