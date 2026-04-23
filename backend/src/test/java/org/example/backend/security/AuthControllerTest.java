package org.example.backend.security;

import org.example.backend.user.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void getMe_returnsUserResponse_whenUserExists() throws Exception {
        // Arrange: User in DB anlegen via /register
        mockMvc.perform(post("/api/auth/register")
                .param("role", "CUSTOMER")
                .with(csrf())
                .with(oauth2Login().attributes(a -> {
                    a.put("id", "gh-123");
                    a.put("login", "testuser");
                    a.put("name", "Test User");
                    a.put("email", "test@example.com");
                }))
        ).andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                        .with(oauth2Login().attributes(a -> a.put("id", "gh-123")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void getMe_returns404_whenUserNotRegistered() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .with(oauth2Login().attributes(a -> a.put("id", "unknown-id")))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getMe_returns401_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_createsCustomer_successfully() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .param("role", "CUSTOMER")
                        .with(csrf())
                        .with(oauth2Login().attributes(a -> {
                            a.put("id", "gh-456");
                            a.put("login", "newuser");
                            a.put("name", "New User");
                            a.put("email", "new@example.com");
                        }))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.name").value("New User"));
    }

    @Test
    void register_createsSeller_successfully() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .param("role", "SELLER")
                        .with(csrf())
                        .with(oauth2Login().attributes(a -> {
                            a.put("id", "gh-789");
                            a.put("login", "selleruser");
                            a.put("name", "Seller User");
                            a.put("email", "seller@example.com");
                        }))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("SELLER"));
    }

    @Test
    void register_returns409_whenUserAlreadyExists() throws Exception {
        // Erst registrieren
        mockMvc.perform(post("/api/auth/register")
                .param("role", "CUSTOMER")
                .with(csrf())
                .with(oauth2Login().attributes(a -> {
                    a.put("id", "gh-dup");
                    a.put("login", "dupuser");
                }))
        ).andExpect(status().isCreated());

        // Nochmal versuchen
        mockMvc.perform(post("/api/auth/register")
                .param("role", "CUSTOMER")
                .with(csrf())
                .with(oauth2Login().attributes(a -> {
                    a.put("id", "gh-dup");
                    a.put("login", "dupuser");
                }))
        ).andExpect(status().isConflict());
    }

    @Test
    void logout_returns204_whenLoggedIn() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .with(oauth2Login())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_returns403_withoutCsrfToken() throws Exception {
        // Ohne gültiges CSRF-Token muss Spring Security 403 zurückgeben
        mockMvc.perform(post("/api/auth/logout")
                        .with(oauth2Login())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void register_returns403_whenRoleIsAdmin() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .param("role", "ADMIN")
                .with(csrf())
                .with(oauth2Login().attributes(a -> a.put("id", "gh-hacker")))
        ).andExpect(status().isForbidden());
    }
}