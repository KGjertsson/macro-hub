package com.kg.macroanalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** On a machine with microk8s, port 31274 is the cluster's database. Running the jar without settings must not reach it. */
class BaseConfigurationTest {

    @Test
    void shouldDefaultToTheComposePostgres_andTheServicesOwnRole() throws IOException {
        // when
        final var properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));

        // then
        assertEquals("jdbc:postgresql://localhost:5432/election", properties.getProperty("spring.datasource.url"));
        assertEquals("election", properties.getProperty("spring.datasource.username"));
    }

    // Parliament is its own service, so this application's own port doesn't answer /parliament/parties
    @Test
    void shouldReadThePartyColoursFromTheParliamentService() throws IOException {
        // when
        final var properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));

        // then
        assertEquals("http://localhost:8084", properties.getProperty("election.parliament.url"));
    }

}
