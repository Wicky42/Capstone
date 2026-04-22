package org.example.backend.user.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Seller extends AppUser {

    private String businessName;
    private String description;
    private String logoUrl;

    private Address address;
    private Address billingAddress;

    private String taxId;
    private String shopId;

    private boolean onboardingCompleted;
}