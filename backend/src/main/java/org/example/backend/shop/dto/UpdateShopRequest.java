package org.example.backend.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateShopRequest(

        @NotBlank(message = "Der Shopname darf nicht leer sein.")
        @Size(min = 3, max = 80, message = "Der Shopname muss zwischen 3 und 80 Zeichen lang sein.")
        String name,

        @NotBlank(message = "Die Beschreibung darf nicht leer sein.")
        @Size(min = 10, max = 500, message = "Die Beschreibung muss zwischen 10 und 500 Zeichen lang sein.")
        String description,

        String logoUrl,
        String headerImageUrl
) {
}
