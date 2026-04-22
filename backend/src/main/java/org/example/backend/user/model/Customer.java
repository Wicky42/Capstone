package org.example.backend.user.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Customer extends AppUser {

    private Address shippingAddress;
    private Address billingAddress;

    private String cartId;
    private List<String> orderIds;
}
