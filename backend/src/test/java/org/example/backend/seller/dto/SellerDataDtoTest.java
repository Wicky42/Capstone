package org.example.backend.seller.dto;

import org.example.backend.user.model.Seller;
import org.example.backend.user.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SellerDataDtoTest {

    @Test
    void from_returnSellerDataDto_WhenCalledWithValidSellerData(){
        Seller seller = Seller.builder()
                .address(new org.example.backend.common.model.Address("Main Street", "123", "12345", "City", "Country"))
                .billingAddress(new org.example.backend.common.model.Address("Billing Street", "456", "67890", "Billing City", "Billing Country"))
                .businessName("John's Shop")
                .taxId("DE123456789")
                .build();

        SellerDataDto dto = SellerDataDto.from(seller);

        assertEquals("John's Shop", dto.businessName());
        assertEquals("Main Street", dto.address().getStreet());
        assertEquals("123", dto.address().getHouseNumber());
        assertEquals("12345", dto.address().getPostalCode());
        assertEquals("City", dto.address().getCity());
        assertEquals("Country", dto.address().getCountry());
        assertEquals("Billing Street", dto.billingAddress().getStreet());
        assertEquals("456", dto.billingAddress().getHouseNumber());
        assertEquals("67890", dto.billingAddress().getPostalCode());
        assertEquals("Billing City", dto.billingAddress().getCity());
        assertEquals("Billing Country", dto.billingAddress().getCountry());
        assertEquals("DE123456789", dto.taxId());
    }

}