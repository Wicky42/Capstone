package org.example.backend.config;

import org.example.backend.user.model.Admin;
import org.example.backend.user.model.User;
import org.example.backend.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    @Value("${app.admin.github-id}")
    private String adminGithubId;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void run(ApplicationArguments args) {
        boolean adminExists = userRepository.existsByOauthProviderAndOauthProviderUserId(
                User.OAuthProvider.GITHUB, adminGithubId
        );

        if (!adminExists) {
            Admin admin = Admin.builder()
                    .oauthProvider(User.OAuthProvider.GITHUB)
                    .oauthProviderUserId(adminGithubId)
                    .role(User.Role.ADMIN)
                    .name("Admin")
                    .email("")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            System.out.println("Initial admin created with GitHub ID: " + adminGithubId);
        }
    }
}


