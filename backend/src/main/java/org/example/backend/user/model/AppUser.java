package org.example.backend.user.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection="users")
public class AppUser {

    enum Role{
        ADMIN,
        SELLER,
        CUSTOMER
    }

    public enum OAuthProvider {
        GITHUB
    }

    @Id
    private String id;
    private Role role;
    private String name;
    private String email;
    private OAuthProvider oauthProvider;
    private String oauthProviderUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
