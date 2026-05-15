package org.example.backend.product.model;

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
    SONSTIGES("Sonstiges");

    private final String label;

    ProductCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}