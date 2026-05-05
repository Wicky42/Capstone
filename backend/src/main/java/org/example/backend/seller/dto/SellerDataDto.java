package org.example.backend.seller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.backend.common.model.Address;
import org.example.backend.user.model.Seller;

public record SellerDataDto(

        @NotBlank(message = "Der Firmenname darf nicht leer sein.")
        @Size(min = 2, max = 120, message = "Der Firmenname muss zwischen 2 und 120 Zeichen lang sein.")
        String businessName,

        @NotNull(message = "Die Geschäftsadresse ist erforderlich.")
        @Valid
        Address address,

        @NotNull(message = "Die Rechnungsadresse ist erforderlich.")
        @Valid
        Address billingAddress,

        @NotBlank(message = "Die Steuer-ID darf nicht leer sein.")
        @Size(min = 3, max = 50, message = "Die Steuer-ID muss zwischen 3 und 50 Zeichen lang sein.")
        String taxId

) {

    public static SellerDataDto from(Seller seller) {
        return new SellerDataDto(
                seller.getBusinessName(),
                seller.getAddress(),
                seller.getBillingAddress(),
                seller.getTaxId()
        );
    }
}