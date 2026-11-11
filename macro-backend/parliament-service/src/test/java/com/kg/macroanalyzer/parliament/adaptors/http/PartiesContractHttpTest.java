package com.kg.macroanalyzer.parliament.adaptors.http;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parliament's half of the contract in contracts/parliament-parties.json: every party it answers
 * with has the fields
 * of that file, as text. ParliamentPartiesClientTest holds election to the same file. Both only
 * use the file and HTTP,
 * so they keep working with the two in different services. A change of the file means a change
 * of the API between them.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PartiesContractHttpTest {

    private static final JsonMapper JSON = new JsonMapper();

    @Value("${local.server.port}")
    int port;

    @Test
    void shouldAnswerWithTheFieldsOfTheContract() throws Exception {
        // given
        final List<String> contractFields;
        try (final var contract =
                     getClass().getResourceAsStream("/contracts/parliament-parties.json")) {
            final var entries = JSON.readTree(contract);
            assertFalse(entries.isEmpty());
            contractFields = new ArrayList<>(entries.get(0).propertyNames());
        }
        assertEquals(List.of("code", "color"), contractFields);

        // when
        final var request = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + port
                                + "/macro-analyzer/parliament/parties"))
                .GET()
                .build();
        final var response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        // then
        assertEquals(200, response.statusCode());
        final JsonNode parties = JSON.readTree(response.body());
        assertTrue(parties.isArray());
        assertFalse(parties.isEmpty());
        for (final var party : parties) {
            for (final var field : contractFields) {
                assertTrue(
                        party.path(field).isString(),
                        () -> "Field %s in %s".formatted(field, party)
                );
                assertFalse(
                        party.path(field).stringValue().isBlank(),
                        () -> "Field %s in %s".formatted(field, party)
                );
            }
            assertTrue(
                    party.path("color").stringValue().matches("#[0-9A-Fa-f]{6}"),
                    () -> "Colour in " + party
            );
        }
    }

}
