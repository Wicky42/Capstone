package org.example.backend.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.example.backend.common.model.Address;
import org.example.backend.order.model.OrderItem;

public record CheckoutRequest(

        @NotNull(message = "Items dürfen nicht null sein.")
        @NotEmpty(message = "Der Warenkorb darf nicht leer sein.")
        OrderItem[] items,

        @NotNull(message = "Die Lieferadresse ist erforderlich.")
        @Valid
        Address shippingAddress,

        @NotNull(message = "Die Rechnungsadresse ist erforderlich.")
        @Valid
        Address billingAddress

) {
}
