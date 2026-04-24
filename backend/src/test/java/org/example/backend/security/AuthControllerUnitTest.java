package org.example.backend.security;

import org.example.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Unit-Tests für den AuthController.
 *
 * Der Branch „oauthUser == null" in getMe() ist im Integrations-Test
 * nicht erreichbar, weil Spring Security unauthentifizierte Anfragen an
 * /api/auth/me (authenticated()) bereits im Filter-Chain mit 401 abweist,
 * bevor die Controller-Methode aufgerufen wird.
 * Dieser Unit-Test stellt sicher, dass der Schutz auch im Controller selbst
 * greift (Defense-in-Depth).
 */
class AuthControllerUnitTest {

    private final UserService userService = mock(UserService.class);
    private final AuthController controller = new AuthController(userService);

    @Test
    void getMe_throws401_whenOauthUserIsNull() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.getMe(null)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void register_throws401_whenOauthUserIsNull() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.register(null, org.example.backend.user.model.User.Role.CUSTOMER)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}

