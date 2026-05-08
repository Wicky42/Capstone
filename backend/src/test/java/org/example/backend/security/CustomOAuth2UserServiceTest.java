package org.example.backend.security;

import org.example.backend.user.model.User;
import org.example.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTest {

    private UserService userService;
    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        service = new CustomOAuth2UserService(userService);
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    /** Erstellt einen OAuth2User mit "id" als nameAttributeKey. */
    private OAuth2User oauthUser(Map<String, Object> attributes) {
        return new DefaultOAuth2User(Collections.emptyList(), attributes, "id");
    }

    /** Erstellt einen OAuth2User mit "login" als nameAttributeKey. */
    private OAuth2User oauthUserByLogin(Map<String, Object> attributes) {
        return new DefaultOAuth2User(Collections.emptyList(), attributes, "login");
    }

    // -------------------------------------------------------------------------
    // 1. Normaler Flow – User nicht in DB → keine Rolle
    // -------------------------------------------------------------------------
    @Test
    void loadUser_returnsUser_withNoRole_whenUserNotFoundInDb() {
        OAuth2User githubUser = oauthUser(Map.of("id", 42, "login", "testuser"));
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        when(userService.findUser(User.OAuthProvider.GITHUB, "42"))
                .thenThrow(new RuntimeException("not found"));

        try (MockedConstruction<DefaultOAuth2UserService> mocked =
                     mockConstruction(DefaultOAuth2UserService.class,
                             (mock, ctx) -> when(mock.loadUser(request)).thenReturn(githubUser))) {

            OAuth2User result = service.loadUser(request);

            assertNotNull(result);
            assertEquals(Integer.valueOf(42), result.getAttribute("id"));
            assertTrue(result.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().startsWith("ROLE_")));
        }
    }

    // -------------------------------------------------------------------------
    // 2. User in DB – SELLER-Rolle wird hinzugefügt
    // -------------------------------------------------------------------------
    @Test
    void loadUser_addsSellerRole_whenUserHasSellerRole() {
        OAuth2User githubUser = oauthUser(Map.of("id", 99, "login", "seller99"));
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(User.Role.SELLER);
        when(userService.findUser(User.OAuthProvider.GITHUB, "99")).thenReturn(mockUser);

        try (MockedConstruction<DefaultOAuth2UserService> mocked =
                     mockConstruction(DefaultOAuth2UserService.class,
                             (mock, ctx) -> when(mock.loadUser(request)).thenReturn(githubUser))) {

            OAuth2User result = service.loadUser(request);

            assertTrue(result.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SELLER")));
        }
    }

    // -------------------------------------------------------------------------
    // 3. User in DB – CUSTOMER-Rolle wird hinzugefügt
    // -------------------------------------------------------------------------
    @Test
    void loadUser_addsCustomerRole_whenUserHasCustomerRole() {
        OAuth2User githubUser = oauthUser(Map.of("id", 7, "login", "customer7"));
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(User.Role.CUSTOMER);
        when(userService.findUser(User.OAuthProvider.GITHUB, "7")).thenReturn(mockUser);

        try (MockedConstruction<DefaultOAuth2UserService> mocked =
                     mockConstruction(DefaultOAuth2UserService.class,
                             (mock, ctx) -> when(mock.loadUser(request)).thenReturn(githubUser))) {

            OAuth2User result = service.loadUser(request);

            assertTrue(result.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
        }
    }

    // -------------------------------------------------------------------------
    // 4. User in DB – ADMIN-Rolle wird hinzugefügt
    // -------------------------------------------------------------------------
    @Test
    void loadUser_addsAdminRole_whenUserIsAdmin() {
        OAuth2User githubUser = oauthUser(Map.of("id", 1, "login", "admin1"));
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(User.Role.ADMIN);
        when(userService.findUser(User.OAuthProvider.GITHUB, "1")).thenReturn(mockUser);

        try (MockedConstruction<DefaultOAuth2UserService> mocked =
                     mockConstruction(DefaultOAuth2UserService.class,
                             (mock, ctx) -> when(mock.loadUser(request)).thenReturn(githubUser))) {

            OAuth2User result = service.loadUser(request);

            assertTrue(result.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }
    }

    // -------------------------------------------------------------------------
    // 5. User in DB – Rolle ist null → keine ROLE_ wird hinzugefügt
    // -------------------------------------------------------------------------
    @Test
    void loadUser_doesNotAddRole_whenUserRoleIsNull() {
        OAuth2User githubUser = oauthUser(Map.of("id", 3, "login", "noRole"));
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(null);
        when(userService.findUser(User.OAuthProvider.GITHUB, "3")).thenReturn(mockUser);

        try (MockedConstruction<DefaultOAuth2UserService> mocked =
                     mockConstruction(DefaultOAuth2UserService.class,
                             (mock, ctx) -> when(mock.loadUser(request)).thenReturn(githubUser))) {

            OAuth2User result = service.loadUser(request);

            assertTrue(result.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().startsWith("ROLE_")));
        }
    }

    // -------------------------------------------------------------------------
    // 6. Kein "id"-Attribut, aber "login" vorhanden
    //    extractOauthId() löst "login" auf, aber DefaultOAuth2User erzwingt
    //    ein "id"-Attribut im Ergebnis → IllegalArgumentException
    //    (dokumentiert eine Einschränkung des hard-kodierten nameAttributeKey)
    // -------------------------------------------------------------------------
    @Test
    void loadUser_throwsIllegalArgument_whenOnlyLoginPresentButIdRequired() {
        OAuth2User githubUser = oauthUserByLogin(Map.of("login", "loginOnlyUser"));
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        when(userService.findUser(any(), any())).thenThrow(new RuntimeException("not found"));

        try (MockedConstruction<DefaultOAuth2UserService> mocked =
                     mockConstruction(DefaultOAuth2UserService.class,
                             (mock, ctx) -> when(mock.loadUser(request)).thenReturn(githubUser))) {

            assertThrows(IllegalArgumentException.class,
                    () -> service.loadUser(request));
        }
    }

    // -------------------------------------------------------------------------
    // 7. Weder "id" noch "login" → OAuth2AuthenticationException
    // -------------------------------------------------------------------------
    @Test
    void loadUser_throwsOAuth2AuthenticationException_whenNoIdOrLoginPresent() {
        OAuth2User githubUser = new DefaultOAuth2User(
                Collections.emptyList(),
                Map.of("email", "user@example.com"),
                "email"
        );
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        try (MockedConstruction<DefaultOAuth2UserService> mocked =
                     mockConstruction(DefaultOAuth2UserService.class,
                             (mock, ctx) -> when(mock.loadUser(request)).thenReturn(githubUser))) {

            assertThrows(OAuth2AuthenticationException.class,
                    () -> service.loadUser(request));
        }
    }

    // -------------------------------------------------------------------------
    // 8. Attribute des Original-OAuth2User bleiben vollständig erhalten
    // -------------------------------------------------------------------------
    @Test
    void loadUser_preservesAllOriginalAttributes() {
        Map<String, Object> attrs = Map.of("id", 5, "login", "user5", "email", "u@example.com");
        OAuth2User githubUser = oauthUser(attrs);
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        when(userService.findUser(any(), any())).thenThrow(new RuntimeException("not found"));

        try (MockedConstruction<DefaultOAuth2UserService> mocked =
                     mockConstruction(DefaultOAuth2UserService.class,
                             (mock, ctx) -> when(mock.loadUser(request)).thenReturn(githubUser))) {

            OAuth2User result = service.loadUser(request);

            assertEquals(Integer.valueOf(5), result.getAttribute("id"));
            assertEquals("user5", result.getAttribute("login"));
            assertEquals("u@example.com", result.getAttribute("email"));
        }
    }
}



