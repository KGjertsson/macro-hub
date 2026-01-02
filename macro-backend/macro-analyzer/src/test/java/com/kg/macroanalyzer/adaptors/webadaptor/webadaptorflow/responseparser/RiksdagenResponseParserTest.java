package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responseparser;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.RiksdagenResponseParser;
import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
