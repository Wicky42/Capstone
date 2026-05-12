export const PRODUCT_CATEGORIES = [
    "Honig",
    "Marmelade & Konfitüre",
    "Gebäck & Kekse",
    "Öle & Essige",
    "Aufstriche & Pasten",
    "Gewürze & Kräuter",
    "Liköre & Schnäpse",
    "Tees & Kräutermischungen",
    "Eingemachtes & Eingeletes",
    "Sonstiges",
] as const;

export type ProductCategory = (typeof PRODUCT_CATEGORIES)[number];

export const CATEGORY_LABELS: Record<ProductCategory, string> = {
    "Honig": "Honig",
    "Marmelade & Konfitüre": "Marmelade",
    "Gebäck & Kekse": "Gebäck",
    "Öle & Essige": "Öle & Essige",
    "Aufstriche & Pasten": "Aufstriche",
    "Gewürze & Kräuter": "Gewürze",
    "Liköre & Schnäpse": "Liköre",
    "Tees & Kräutermischungen": "Tees",
    "Eingemachtes & Eingeletes": "Eingemachtes",
    "Sonstiges": "Sonstiges",
};

