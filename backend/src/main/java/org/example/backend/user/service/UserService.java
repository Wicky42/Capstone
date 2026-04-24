package org.example.backend.user.service;

import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.user.dto.UserResponse;
import org.example.backend.user.model.*;
import org.example.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

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

        userRepository.save(newUser);
        return newUser;
    }

    public User findUser(User.OAuthProvider oauthProvider, String oauthId) {
        return userRepository.findByOauthProviderAndOauthProviderUserId(oauthProvider, oauthId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }


    private User getCurrentUser (){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User appUser)) {
            throw new ForbiddenAccessException("Kein eingeloggter Benutzer gefunden.");
        }
        return userRepository.findById(appUser.getId())
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));
    }

    private User getCurrentUserByRole(User.Role role){
        User appUser = getCurrentUser();
        if(appUser.getRole() != role){
            throw new ForbiddenAccessException("Unerlaubter Zugriff. Nur Seller dürfen diesen Bereich nutzen.");
        }
        return appUser;
    }

    public Seller getCurrentSeller(){
        User currentUser = getCurrentUserByRole(User.Role.SELLER);

        if (!(currentUser instanceof Seller seller)) {
            throw new IllegalStateException("Benutzer hat Rolle SELLER, ist aber kein Seller-Typ.");
        }

        return seller;
    }
}
