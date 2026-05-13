package org.example.backend.security;

import lombok.RequiredArgsConstructor;
import org.example.backend.user.model.User;
import org.example.backend.user.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(userRequest);

        String oauthId = extractOauthId(oauthUser);

        Set<GrantedAuthority> authorities = new HashSet<>(oauthUser.getAuthorities());

        try {
            User user = userService.findUser(User.OAuthProvider.GITHUB, oauthId);

            if (user.getRole() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            }
        } catch (Exception ignored) {
            // User ist evtl. noch nicht registriert.
            // Dann bekommt er noch keine ROLE_CUSTOMER / ROLE_SELLER / ROLE_ADMIN.
        }

        return new DefaultOAuth2User(
                authorities,
                oauthUser.getAttributes(),
                "id"
        );
    }

    private String extractOauthId(OAuth2User oauthUser) {
        Object id = oauthUser.getAttribute("id");
        if (id != null) return id.toString();

        Object login = oauthUser.getAttribute("login");
        if (login != null) return login.toString();

        throw new OAuth2AuthenticationException("Cannot determine OAuth user id");
    }
}