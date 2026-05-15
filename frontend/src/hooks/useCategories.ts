import { useEffect, useState } from "react";
import { storefrontService } from "../services/storefrontService";
import type { CategoryOption } from "../types/category";

// Modul-Level-Cache: wird einmalig geladen und dann wiederverwendet
let cachedCategories: CategoryOption[] | null = null;

type UseCategoriesResult = {
    categories: CategoryOption[];
    loading: boolean;
    error: string | null;
};

export function useCategories(): UseCategoriesResult {
    // State direkt aus Cache initialisieren – kein synchrones setState im Effect nötig
    const [categories, setCategories] = useState<CategoryOption[]>(() => cachedCategories ?? []);
    const [loading, setLoading] = useState(() => cachedCategories === null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        // Cache bereits vorhanden → nichts tun
        if (cachedCategories !== null) return;

        storefrontService
            .getCategories()
            .then((data) => {
                cachedCategories = data;
                setCategories(data);
                setError(null);
            })
            .catch(() => {
                setCategories([]);
                setError("Kategorien konnten nicht geladen werden.");
            })
            .finally(() => setLoading(false));
    }, []);

    return { categories, loading, error };
}
