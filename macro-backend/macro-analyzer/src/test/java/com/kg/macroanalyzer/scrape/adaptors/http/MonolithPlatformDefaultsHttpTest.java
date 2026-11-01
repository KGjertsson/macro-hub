package com.kg.macroanalyzer.scrape.adaptors.http;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** What macro-platform leaves as it was for an application that doesn't opt in, as the monolith doesn't. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MonolithPlatformDefaultsHttpTest {

    @Value("${local.server.port}")
    int port;
    @Autowired
    DSLContext dslContext;

    @Test
    void shouldKeepTheManualEndpoints_onTheOldPaths() throws Exception {
        // given
        // An unknown name is accepted and ignored, so this doesn't queue a real scrape
        final var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/macro-analyzer/enqueue?name=unknown"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        // when
        final var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());

        // then
        assertEquals(202, response.statusCode());
    }

    @Test
    void shouldNotRunThePlatformMigrations() {
        assertFalse(dslContext.fetchExists(
                dslContext.selectOne().from("pg_tables").where("tablename = 'flyway_platform_schema_history'")
        ));
    }

}
