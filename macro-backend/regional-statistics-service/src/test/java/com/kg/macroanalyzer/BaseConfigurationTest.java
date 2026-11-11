package com.kg.macroanalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseConfigurationTest {

    @Test
    void shouldDefaultToTheComposePostgres_andTheServicesOwnRole() throws IOException {
        // when
        final var properties = PropertiesLoaderUtils.loadProperties(
                new ClassPathResource("application.properties")
        );

        // then
        assertEquals(
                "jdbc:postgresql://localhost:5432/regional_statistics",
                properties.getProperty("spring.datasource.url")
        );
        assertEquals("regional_statistics", properties.getProperty("spring.datasource.username"));
    }

}
