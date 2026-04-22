package org.example.backend.user.service;

import org.example.backend.user.model.*;
import org.example.backend.user.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User.OAuthProvider oauthProvider, String oauthId, String name, String email, User.Role role) {
        if (userRepository.existsByOauthProviderAndOauthProviderUserId(oauthProvider, oauthId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        LocalDateTime now = LocalDateTime.now();

        User newUser = switch (role) {
            case SELLER -> Seller.builder()
                    .oauthProvider(oauthProvider)
                    .oauthProviderUserId(oauthId)
                    .name(name)
                    .email(email)
                    .role(User.Role.SELLER)
                    .onboardingCompleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            case CUSTOMER -> Customer.builder()
                    .oauthProvider(oauthProvider)
                    .oauthProviderUserId(oauthId)
                    .name(name)
                    .email(email)
                    .role(User.Role.CUSTOMER)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            case ADMIN -> Admin.builder()
                    .oauthProvider(oauthProvider)
                    .oauthProviderUserId(oauthId)
                    .name(name)
                    .email(email)
                    .role(User.Role.ADMIN)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        };

        return userRepository.save(newUser);
    }

    public User findUser(User.OAuthProvider oauthProvider, String oauthId) {
        return userRepository.findByOauthProviderAndOauthProviderUserId(oauthProvider, oauthId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
