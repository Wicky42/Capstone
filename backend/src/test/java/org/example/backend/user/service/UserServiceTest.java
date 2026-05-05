package org.example.backend.user.service;

import org.example.backend.common.exception.ForbiddenAccessException;
import org.example.backend.user.model.*;
import org.example.backend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─── Hilfsmethode ────────────────────────────────────────────────────────

    private void setupSecurityContext(Object oauthId) {
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(principal.getAttribute("id")).thenReturn(oauthId);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createUser_createsCustomer_whenRoleIsCustomer() {
        when(userRepository.existsByOauthProviderAndOauthProviderUserId(any(), any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(
                User.OAuthProvider.GITHUB, "gh-1", "Max", "max@example.com", User.Role.CUSTOMER
        );

        assertThat(result).isInstanceOf(Customer.class);
        assertThat(result.getRole()).isEqualTo(User.Role.CUSTOMER);
        assertThat(result.getName()).isEqualTo("Max");
    }

    @Test
    void createUser_createsSeller_whenRoleIsSeller() {
        when(userRepository.existsByOauthProviderAndOauthProviderUserId(any(), any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(
                User.OAuthProvider.GITHUB, "gh-2", "Anna", "anna@example.com", User.Role.SELLER
        );

        assertThat(result).isInstanceOf(Seller.class);
        assertThat(((Seller) result).isOnboardingCompleted()).isFalse();
    }

    @Test
    void createUser_throwsConflict_whenUserAlreadyExists() {
        when(userRepository.existsByOauthProviderAndOauthProviderUserId(any(), any())).thenReturn(true);

        assertThatThrownBy(() ->
                userService.createUser(User.OAuthProvider.GITHUB, "gh-dup", "X", "", User.Role.CUSTOMER)
        ).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void findUser_returnsUser_whenFound() {
        Customer customer = Customer.builder()
                .oauthProvider(User.OAuthProvider.GITHUB)
                .oauthProviderUserId("gh-3")
                .role(User.Role.CUSTOMER)
                .name("Lisa")
                .email("lisa@example.com")
                .build();

        when(userRepository.findByOauthProviderAndOauthProviderUserId(any(), any()))
                .thenReturn(Optional.of(customer));

        User result = userService.findUser(User.OAuthProvider.GITHUB, "gh-3");

        assertThat(result.getName()).isEqualTo("Lisa");
    }

    @Test
    void findUser_throws404_whenNotFound() {
        when(userRepository.findByOauthProviderAndOauthProviderUserId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUser(User.OAuthProvider.GITHUB, "gh-none"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    // ─── getCurrentUser ───────────────────────────────────────────────────────

    @Test
    void getCurrentUser_returnsUser_whenAuthenticatedAndUserExists() {
        setupSecurityContext(42);

        Customer customer = Customer.builder()
                .oauthProvider(User.OAuthProvider.GITHUB)
                .oauthProviderUserId("42")
                .role(User.Role.CUSTOMER)
                .name("Max")
                .email("max@example.com")
                .build();

        when(userRepository.findByOauthProviderAndOauthProviderUserId(User.OAuthProvider.GITHUB, "42"))
                .thenReturn(Optional.of(customer));

        User result = userService.getCurrentUser();

        assertThat(result.getName()).isEqualTo("Max");
        assertThat(result.getRole()).isEqualTo(User.Role.CUSTOMER);
    }

    @Test
    void getCurrentUser_throwsForbidden_whenNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getCurrentUser_throwsForbidden_whenAuthenticationIsNull() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getCurrentUser_throwsForbidden_whenPrincipalIsNotOAuth2() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("not-an-oauth2-principal");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getCurrentUser_throwsForbidden_whenOAuthUserIdIsNull() {
        setupSecurityContext(null);

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void getCurrentUser_throwsRuntimeException_whenUserNotFoundInRepository() {
        setupSecurityContext(99);

        when(userRepository.findByOauthProviderAndOauthProviderUserId(User.OAuthProvider.GITHUB, "99"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("nicht gefunden");
    }

    // ─── getCurrentSeller ─────────────────────────────────────────────────────

    @Test
    void getCurrentSeller_returnsSeller_whenUserHasRoleSeller() {
        setupSecurityContext(7);

        Seller seller = Seller.builder()
                .oauthProvider(User.OAuthProvider.GITHUB)
                .oauthProviderUserId("7")
                .role(User.Role.SELLER)
                .name("Anna")
                .email("anna@example.com")
                .build();

        when(userRepository.findByOauthProviderAndOauthProviderUserId(User.OAuthProvider.GITHUB, "7"))
                .thenReturn(Optional.of(seller));

        Seller result = userService.getCurrentSeller();

        assertThat(result.getName()).isEqualTo("Anna");
    }

    @Test
    void getCurrentSeller_throwsForbidden_whenUserIsNotSeller() {
        setupSecurityContext(8);

        Customer customer = Customer.builder()
                .oauthProvider(User.OAuthProvider.GITHUB)
                .oauthProviderUserId("8")
                .role(User.Role.CUSTOMER)
                .name("Bob")
                .email("bob@example.com")
                .build();

        when(userRepository.findByOauthProviderAndOauthProviderUserId(User.OAuthProvider.GITHUB, "8"))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> userService.getCurrentSeller())
                .isInstanceOf(ForbiddenAccessException.class);
    }
}