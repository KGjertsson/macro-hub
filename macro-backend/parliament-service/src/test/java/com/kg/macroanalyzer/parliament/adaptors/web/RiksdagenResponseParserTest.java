package com.kg.macroanalyzer.parliament.adaptors.web;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RiksdagenResponseParserTest {

    private RiksdagenResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new RiksdagenResponseParser();
    }

    @Test
    void shouldParseValidResponseCorrectly() throws ScrapeException {
        // given
        String jsonResponse = """
                {
                  "personlista": {
                    "@systemdatum": "2023-01-01",
                    "@hits": "1",
                    "person": [
                      {
                        "tilltalsnamn": "John",
                        "efternamn": "Doe",
                        "parti": "S"
                      }
                    ]
                  }
                }
                """;

        // when
        List<MemberOfParliament> result = parser.parse(jsonResponse).toList();

        // then
        assertEquals(1, result.size());
        assertEquals("John", result.getFirst().tilltalsnamn());
        assertEquals("Doe", result.getFirst().efternamn());
        assertEquals("S", result.getFirst().parti());
    }

    @Test
    void shouldReturnEmptyStream_whenPersonListaIsNull() throws ScrapeException {
        // given
        String jsonResponse = """
                {
                  "personlista": null
                }
                """;

        // when
        List<MemberOfParliament> result = parser.parse(jsonResponse).toList();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyStream_whenPersonIsNull() throws ScrapeException {
        // given
        String jsonResponse = """
                {
                  "personlista": {
                    "person": null
                  }
                }
                """;

        // when
        List<MemberOfParliament> result = parser.parse(jsonResponse).toList();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowScrapeException_whenInvalidJson() {
        // given
        String invalidJson = "{ invalid json }";

        // when && then
        assertThrows(ScrapeException.class, () -> parser.parse(invalidJson));
    }

    @Test
    void shouldParseRealResponse_withAssignmentListAndEmptyAssignments() throws ScrapeException {
        // given
        final var jsonResponse = new TestJsonReader().readResource("json/riksdagen/personlista_sample.json");

        // when
        final var result = parser.parse(jsonResponse).toList();

        // then
        assertEquals(2, result.size());
        final var member = result.getFirst();
        assertEquals("0588277881928", member.intressent_id());
        assertEquals(3, member.assignments().size());
        assertEquals("UbU", member.assignments().get(1).organ_kod());
        assertEquals(2, member.details().size());
        assertNull(result.get(1).personuppdrag());
        assertTrue(result.get(1).assignments().isEmpty());
    }

    @Test
    void shouldParseSingleAssignmentObject_asOneElementList() throws ScrapeException {
        // given
        String jsonResponse = """
                {
                  "personlista": {
                    "person": {
                      "intressent_id": "123",
                      "unknown_field": "ignored",
                      "personuppdrag": {
                        "uppdrag": {"typ": "kammaruppdrag", "organ_kod": "kam", "from": "2022-09-26", "tom": ""}
                      },
                      "personuppgift": {
                        "uppgift": {"kod": "HarBild", "uppgift": ["true"], "typ": "bilder"}
                      }
                    }
                  }
                }
                """;

        // when
        final var result = parser.parse(jsonResponse).toList();

        // then
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().assignments().size());
        assertEquals("kam", result.getFirst().assignments().getFirst().organ_kod());
        assertEquals(1, result.getFirst().details().size());
    }

}
