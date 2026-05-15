package org.example.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Konfiguration für die Zentrallager-Adresse.
 * Werte werden aus application.properties unter dem Präfix "warehouse" gelesen.
 *
 * Beispiel:
 *   warehouse.street=Lagerstraße
 *   warehouse.house-number=1
 *   warehouse.postal-code=10115
 *   warehouse.city=Berlin
 *   warehouse.country=Deutschland
 */
@Component
@ConfigurationProperties(prefix = "warehouse")
@Getter
@Setter
public class WarehouseProperties {

    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String country;
}

