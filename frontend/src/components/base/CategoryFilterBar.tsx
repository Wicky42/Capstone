import type { FC } from "react";
import { PRODUCT_CATEGORIES } from "../../types/category";
import "./CategoryFilterBar.css";

type Props = {
    selected: string | undefined;
    onChange: (category: string | undefined) => void;
};

const CategoryFilterBar: FC<Props> = ({ selected, onChange }) => (
    <div className="category-filter-bar" role="group" aria-label="Kategorie filtern">
        <button
            className={`chip category-filter-bar__chip${!selected ? " category-filter-bar__chip--active" : ""}`}
            onClick={() => onChange(undefined)}
            type="button"
        >
            Alle
        </button>
        {PRODUCT_CATEGORIES.map((cat) => (
            <button
                key={cat}
                className={`chip category-filter-bar__chip${selected === cat ? " category-filter-bar__chip--active" : ""}`}
                onClick={() => onChange(selected === cat ? undefined : cat)}
                type="button"
            >
                {cat}
            </button>
        ))}
    </div>
);

export default CategoryFilterBar;

