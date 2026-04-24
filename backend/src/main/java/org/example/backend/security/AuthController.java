package org.example.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.user.model.User;
import org.example.backend.user.dto.UserResponse;
import org.example.backend.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Gibt den eingeloggten User zurück.
     * Wirft 404, wenn der User noch nicht registriert ist → Frontend leitet zur RegisterPage weiter.
     */
    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal OAuth2User oauthUser) {
        if (oauthUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        User user = userService.findUser(User.OAuthProvider.GITHUB, extractOauthId(oauthUser));
        return UserResponse.from(user);
    }

    /**
     * Registriert einen neuen User mit der gewählten Rolle.
     * Wird nach dem ersten GitHub-OAuth-Login aufgerufen.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @AuthenticationPrincipal OAuth2User oauthUser,
            @RequestParam User.Role role
    ) {
        if (oauthUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (role == User.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role cannot be self-assigned");
        }

        String oauthId = extractOauthId(oauthUser);
        String name = extractAttr(oauthUser, "name", extractAttr(oauthUser, "login", oauthId));
        String email = extractAttr(oauthUser, "email", "");

        User user = userService.createUser(User.OAuthProvider.GITHUB, oauthId, name, email, role);
        return UserResponse.from(user);
    }

    // --- helpers ---

    /**
     * Meldet den User ab: invalidiert die Session und löscht den Security-Context.
     * Gibt 204 No Content zurück (kein Redirect) – Frontend steuert die Weiterleitung.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    /**
     * Erzwingt die Generierung des CSRF-Tokens und setzt den XSRF-TOKEN-Cookie.
     * Muss vom Frontend beim Start aufgerufen werden, damit der Cookie existiert.
     */
    @GetMapping("/csrf")
    public void csrf(HttpServletRequest request) {
        // Das bloße Zugreifen auf das CsrfToken-Attribut genügt:
        // CookieCsrfTokenRepository schreibt daraufhin den XSRF-TOKEN-Cookie in die Response.
        request.getAttribute(CsrfToken.class.getName());
    }

    private String extractOauthId(OAuth2User oauthUser) {
        Object id = oauthUser.getAttribute("id");
        if (id != null) return id.toString();
        Object login = oauthUser.getAttribute("login");
        if (login != null) return login.toString();
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot determine OAuth user id");
    }

    private String extractAttr(OAuth2User oauthUser, String key, String fallback) {
        Object val = oauthUser.getAttribute(key);
        return val != null ? val.toString() : fallback;
    }
}
