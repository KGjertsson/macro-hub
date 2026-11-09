package com.kg.macroanalyzer.scrape.adaptors.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.observation.SecurityObservationSettings;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class ScrapeSecurityConfiguration {

    private static final String ISSUER = "macro-hub-auth";
    private static final String AUDIENCE = "macro-hub";
    private static final String ROLES_CLAIM = "roles";
    private static final int KEY_FETCH_TIMEOUT_MILLIS = 5_000;

    // Only the scrape paths: with no filter chain on the public routes, a stale token in a request can't turn a read into a 401
    @Bean
    SecurityFilterChain scrapeSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/macro-analyzer/scrape/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests.anyRequest().hasRole("ADMIN"))
                .oauth2ResourceServer(server -> server.jwt(jwt -> jwt.jwtAuthenticationConverter(rolesAsAuthorities())))
                .build();
    }

    // FilterChainProxy wraps every request, also those no chain matches, so it would add spans to each traced public read
    @Bean
    SecurityObservationSettings noSecurityObservations() {
        return SecurityObservationSettings.noObservations();
    }

    // The keys are fetched at the first token, so a service starts without auth-service
    @Bean
    JwtDecoder jwtDecoder(@Value("${auth.jwks-uri}") String jwksUri) {
        final var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(KEY_FETCH_TIMEOUT_MILLIS);
        requestFactory.setReadTimeout(KEY_FETCH_TIMEOUT_MILLIS);
        final var decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .restOperations(new RestTemplate(requestFactory))
                .build();
        decoder.setJwtValidator(accessTokenValidator());

        return decoder;
    }

    static OAuth2TokenValidator<Jwt> accessTokenValidator() {
        return new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(ISSUER),
                new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, audience -> audience != null && audience.contains(AUDIENCE)));
    }

    private static JwtAuthenticationConverter rolesAsAuthorities() {
        final var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(ROLES_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");
        final var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);

        return converter;
    }

}
