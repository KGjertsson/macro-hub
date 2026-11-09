package com.kg.macroanalyzer.auth.adaptors.config;

import com.kg.macroanalyzer.auth.adaptors.jwt.NimbusAccessTokenAdaptor;
import com.kg.macroanalyzer.auth.application.port.out.AccessTokenPort;
import com.kg.macroanalyzer.auth.application.port.out.AuthRepositoryPort;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
public class AuthJwtConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    AccessTokenPort accessTokenPort(
            AuthRepositoryPort repository,
            Clock clock,
            @Value("${auth.access-token-ttl}") Duration accessTokenTtl
    ) throws JOSEException, ParseException {
        final var stored = repository.signingKeysNewestFirst();
        if (stored.isEmpty()) {
            final var generated = new ECKeyGenerator(Curve.P_256).keyID(UUID.randomUUID().toString()).generate();
            repository.saveSigningKey(generated.getKeyID(), generated.toJSONString(), clock.instant());

            return new NimbusAccessTokenAdaptor(generated, List.of(generated), accessTokenTtl);
        }

        final var keys = new ArrayList<JWK>();
        for (final var key : stored) {
            keys.add(ECKey.parse(key));
        }

        return new NimbusAccessTokenAdaptor(keys.getFirst().toECKey(), keys, accessTokenTtl);
    }

}
