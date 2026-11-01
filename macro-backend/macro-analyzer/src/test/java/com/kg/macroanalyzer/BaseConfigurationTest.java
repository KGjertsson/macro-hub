package com.kg.macroanalyzer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Port 31274 is the microk8s cluster's database, on localhost and on the cluster's address. An application started
 * with its defaults, or with the dev profile, must not reach it and migrate it.
 */
class BaseConfigurationTest {

    @ParameterizedTest
    @ValueSource(strings = {"application.properties", "application-dev.properties"})
    void shouldDefaultToTheComposePostgres(final String file) throws IOException {
        // when
        final var properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource(file));

        // then
        assertEquals("jdbc:postgresql://localhost:5432/macroanalyzer", properties.getProperty("spring.datasource.url"));
    }

}
