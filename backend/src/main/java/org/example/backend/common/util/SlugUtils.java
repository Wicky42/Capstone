package org.example.backend.common.util;

import java.text.Normalizer;

/**
 * Utility class for generating URL-friendly slugs.
 * Uses NFD normalisation to handle umlauts and diacritics (ä→a, ö→o, ü→u, etc.).
 */
public final class SlugUtils {

    private SlugUtils() {}

    /**
     * Converts a raw name into a URL-friendly slug segment.
     * Examples:
     * <ul>
     *   <li>"Honigstube Müller" → "honigstube-muller"</li>
     *   <li>"Bio-Waldhonig 500g!" → "bio-waldhonig-500g"</li>
     * </ul>
     */
    public static String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein.");
        }

        // NFD decomposition: ä → a + combining diacritic, ü → u + …
        String normalized = Normalizer.normalize(name.toLowerCase().trim(), Normalizer.Form.NFD);

        StringBuilder slug = new StringBuilder(normalized.length());
        boolean prevDash = false;

        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            int type = Character.getType(c);

            // Drop combining/diacritic marks produced by NFD decomposition
            if (type == Character.NON_SPACING_MARK
                    || type == Character.COMBINING_SPACING_MARK
                    || type == Character.ENCLOSING_MARK) {
                continue;
            }

            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                slug.append(c);
                prevDash = false;
            } else if (!prevDash && !slug.isEmpty()) {
                // Collapse any run of non-alphanumeric characters into a single dash
                slug.append('-');
                prevDash = true;
            }
        }

        // Remove trailing dash
        int end = slug.length();
        while (end > 0 && slug.charAt(end - 1) == '-') {
            end--;
        }

        return slug.substring(0, end);
    }
}

