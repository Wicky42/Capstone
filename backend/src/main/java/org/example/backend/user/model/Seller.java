package org.example.backend.user.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "users")
public class Seller extends User {

    private String businessName;
    private String description;
    private String logoUrl;

    private Address address;
    private Address billingAddress;

    private String taxId;
    private String shopId;

    private boolean onboardingCompleted;
}