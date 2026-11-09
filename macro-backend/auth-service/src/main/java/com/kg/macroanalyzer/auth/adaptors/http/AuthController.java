package com.kg.macroanalyzer.auth.adaptors.http;

import com.kg.macroanalyzer.auth.application.port.in.AuthCommands;
import com.kg.macroanalyzer.auth.application.port.in.Authenticated;
import com.kg.macroanalyzer.auth.domain.DisplayNameTakenException;
import com.kg.macroanalyzer.auth.domain.InvalidCredentialsException;
import com.kg.macroanalyzer.auth.domain.InvalidEmailTokenException;
import com.kg.macroanalyzer.auth.domain.InvalidInputException;
import com.kg.macroanalyzer.auth.domain.InvalidSessionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/macro-analyzer/auth")
public class AuthController {

    private static final String SESSION_COOKIE = "macro_session";
    private static final String SESSION_COOKIE_PATH = "/macro-analyzer/auth";

    private final AuthCommands auth;
    private final boolean secureCookie;

    public AuthController(AuthCommands auth, @Value("${auth.cookie.secure}") boolean secureCookie) {
        this.auth = auth;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody EmailRequest request) {
        auth.requestRegistration(request.email());

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/register/complete")
    public ResponseEntity<Void> completeRegistration(@RequestBody CompleteRegistrationRequest request) {
        auth.completeRegistration(request.token(), request.displayName(), request.password());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody EmailRequest request) {
        auth.requestPasswordReset(request.email());

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/password-reset/complete")
    public ResponseEntity<Void> completePasswordReset(@RequestBody CompletePasswordResetRequest request) {
        auth.completePasswordReset(request.token(), request.password());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticatedResponse> login(@RequestBody LoginRequest request) {
        final var login = auth.login(request.email(), request.password());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, sessionCookie(login.sessionToken(), login.sessionTtl()).toString())
                .body(response(login.authenticated()));
    }

    @PostMapping("/token")
    public ResponseEntity<AuthenticatedResponse> token(@CookieValue(name = SESSION_COOKIE, required = false) String sessionToken) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response(auth.refresh(sessionToken)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = SESSION_COOKIE, required = false) String sessionToken) {
        auth.logout(sessionToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessionCookie("", Duration.ZERO).toString())
                .build();
    }

    @GetMapping(value = "/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public String publicKeys() {
        return auth.publicKeys();
    }

    @ExceptionHandler(InvalidInputException.class)
    ResponseEntity<ErrorResponse> invalidInput(InvalidInputException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("invalid_" + e.getField()));
    }

    @ExceptionHandler(InvalidEmailTokenException.class)
    ResponseEntity<ErrorResponse> invalidEmailToken() {
        return ResponseEntity.badRequest().body(new ErrorResponse("invalid_or_expired_token"));
    }

    @ExceptionHandler(DisplayNameTakenException.class)
    ResponseEntity<ErrorResponse> displayNameTaken() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("display_name_taken"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ErrorResponse> invalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("invalid_credentials"));
    }

    @ExceptionHandler(InvalidSessionException.class)
    ResponseEntity<ErrorResponse> invalidSession() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, sessionCookie("", Duration.ZERO).toString())
                .body(new ErrorResponse("invalid_session"));
    }

    private ResponseCookie sessionCookie(String value, Duration maxAge) {
        // Strict: only requests from the app's own pages carry it, which is what stands in for a CSRF token on /token and /logout
        return ResponseCookie.from(SESSION_COOKIE, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path(SESSION_COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }

    private static AuthenticatedResponse response(Authenticated authenticated) {
        final var user = authenticated.user();

        return new AuthenticatedResponse(
                authenticated.accessToken().value(),
                authenticated.accessToken().expiresIn().toSeconds(),
                new AuthenticatedResponse.UserResponse(user.id(), user.email(), user.displayName(), user.role()));
    }

}
