package org.example.backend.user.service;

import org.example.backend.user.model.*;
import org.example.backend.user.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
}