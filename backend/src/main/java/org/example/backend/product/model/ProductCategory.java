package org.example.backend.product.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProductCategory {
    HONIG("Honig"),
    MARMELADE_KONFITUERE("Marmelade & Konfitüre"),
    GEBAECK_KEKSE("Gebäck & Kekse"),
    OELE_ESSIGE("Öle & Essige"),
    AUFSTRICHE_PASTEN("Aufstriche & Pasten"),
    GEWURZE_KRAEUTER("Gewürze & Kräuter"),
    LIKOERE_SCHNAPSE("Liköre & Schnäpse"),
    TEES_KRAEUTERMISCHUNGEN("Tees & Kräutermischungen"),
    EINGEMACHTES_EINGELEGTES("Eingemachtes & Eingelegtes"),
    OBST("Obst"),
    SONSTIGES("Sonstiges");

    private final String label;

    ProductCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static ProductCategory fromValue(String value) {
        if (value == null) return null;
        for (ProductCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(value) || cat.label.equalsIgnoreCase(value)) {
                return cat;
            }
        }
        throw new IllegalArgumentException("Unbekannte Kategorie: " + value);
    }
}