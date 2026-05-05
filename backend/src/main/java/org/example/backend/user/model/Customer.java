package org.example.backend.user.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.backend.common.model.Address;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "users")
public class Customer extends User {

    private Address shippingAddress;
    private Address billingAddress;

    private String cartId;
    private List<String> orderIds;
}
