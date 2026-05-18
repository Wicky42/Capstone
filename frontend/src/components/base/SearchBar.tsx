import { type FC, useEffect, useRef, useState } from "react";
import "./SearchBar.css";

type Props = {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
};

const SearchBar: FC<Props> = ({ value, onChange, placeholder = "Produkte suchen…" }) => {
    const [localValue, setLocalValue] = useState(value);
    const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => {
        setLocalValue(value);
    }, [value]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newVal = e.target.value;
        setLocalValue(newVal);
        if (debounceRef.current) clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => {
            onChange(newVal);
        }, 300);
    };

    return (
        <div className="search-bar">
            <span className="search-bar__icon" aria-hidden="true">🔍</span>
            <input
                className="search-bar__input input"
                type="search"
                value={localValue}
                onChange={handleChange}
                placeholder={placeholder}
                aria-label={placeholder}
            />
        </div>
    );
};

export default SearchBar;

