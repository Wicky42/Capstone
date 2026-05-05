package org.example.backend.user.controller;

import org.example.backend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void getAllUsers_returnsUserList_whenAdmin() throws Exception {
        // Einen normalen User registrieren
        mockMvc.perform(post("/api/auth/register")
                .param("role", "CUSTOMER")
                .with(csrf())
                .with(oauth2Login().attributes(a -> {
                    a.put("id", "gh-user1");
                    a.put("login", "user1");
                    a.put("name", "User One");
                    a.put("email", "one@example.com");
                }))
        );

        // Admin-Anfrage: oauth2Login mit ROLE_ADMIN Authority, damit hasRole("ADMIN") greift
        mockMvc.perform(get("/api/admin/users")
                        .with(oauth2Login()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                .attributes(a -> {
                                    a.put("id", "test-id");
                                    a.put("login", "admin");
                                }))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllUsers_returns401_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}