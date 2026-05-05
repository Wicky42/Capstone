package org.example.backend.common.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String country;
}