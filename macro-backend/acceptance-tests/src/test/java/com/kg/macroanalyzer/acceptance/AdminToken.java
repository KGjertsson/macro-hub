package com.kg.macroanalyzer.acceptance;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

// Most journeys scrape to set up their data, and booting auth-service in each of their stacks would be most of their time.
// They get a token signed here, and their services read this key from the stub. AuthAcceptanceTest uses the real auth-service
public final class AdminToken {

    public static final String KEYS_PATH = "/test-auth/jwks.json";

    private static final Duration LONGER_THAN_THE_SUITE = Duration.ofHours(2);
    private static final ECKey KEY = generateKey();
    private static final String VALUE = sign();

    private AdminToken() {
    }

    public static String value() {
        return VALUE;
    }

    public static String publicKeys() {
        return new JWKSet(KEY).toString(true);
    }

    private static ECKey generateKey() {
        try {
            return new ECKeyGenerator(Curve.P_256).keyID("acceptance").generate();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sign() {
        final var now = Instant.now();
        final var claims = new JWTClaimsSet.Builder()
                .issuer("macro-hub-auth")
                .audience("macro-hub")
                .subject("acceptance-admin")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(LONGER_THAN_THE_SUITE)))
                .claim("roles", List.of("ADMIN"))
                .build();
        final var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(KEY.getKeyID()).build(), claims);
        try {
            jwt.sign(new ECDSASigner(KEY));
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }

        return jwt.serialize();
    }

}
