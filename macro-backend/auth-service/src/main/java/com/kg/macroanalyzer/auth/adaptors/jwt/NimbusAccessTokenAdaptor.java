package com.kg.macroanalyzer.auth.adaptors.jwt;

import com.kg.macroanalyzer.auth.application.port.out.AccessTokenPort;
import com.kg.macroanalyzer.auth.domain.AccessToken;
import com.kg.macroanalyzer.auth.domain.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NimbusAccessTokenAdaptor implements AccessTokenPort {

    public static final String ISSUER = "macro-hub-auth";
    public static final String AUDIENCE = "macro-hub";
    public static final String ROLES_CLAIM = "roles";
    public static final String NAME_CLAIM = "name";

    private final ECKey signingKey;
    private final String publicKeys;
    private final Duration ttl;

    public NimbusAccessTokenAdaptor(ECKey signingKey, List<JWK> allKeys, Duration ttl) {
        this.signingKey = signingKey;
        this.publicKeys = new JWKSet(allKeys).toString(true);
        this.ttl = ttl;
    }

    @Override
    public AccessToken issue(User user, Instant now) {
        final var claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .subject(user.id().toString())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ttl)))
                .claim(ROLES_CLAIM, List.of(user.role().name()))
                .claim(NAME_CLAIM, user.displayName())
                .build();
        final var header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(JOSEObjectType.JWT)
                .keyID(signingKey.getKeyID())
                .build();
        final var jwt = new SignedJWT(header, claims);
        try {
            jwt.sign(new ECDSASigner(signingKey));
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not sign an access token", e);
        }

        return new AccessToken(jwt.serialize(), ttl);
    }

    @Override
    public String publicKeys() {
        return publicKeys;
    }

}
