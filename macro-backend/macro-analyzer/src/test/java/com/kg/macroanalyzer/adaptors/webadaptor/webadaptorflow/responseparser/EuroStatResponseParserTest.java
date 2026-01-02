package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responseparser;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.EuroStatResponseParser;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EuroStatResponseParserTest {

    private EuroStatResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new EuroStatResponseParser();
    }

    @Test
    void shouldParseValidResponseCorrectly() throws ScrapeException {
        // given
        String jsonResponse = """
                {
                  "version": "2.0",
                  "class": "dataset",
                  "label": "Test Dataset",
                  "value": {
                    "0": 100.5,
                    "1": 200.75
                  },
                  "dimension": {
                    "time": {
                      "label": "Time",
                      "category": {
                        "index": {
                          "2021": 0,
                          "2022": 1
                        }
                      }
                    }
                  }
                }
                """;

        // when
        List<MacroPoint> result = parser.parse(jsonResponse).toList();

        // then
        assertEquals(2, result.size());

        MacroPoint point1 = result.stream()
                .filter(p -> p.date().equals(LocalDate.of(2021, 1, 1)))
                .findFirst()
                .orElseThrow();
        assertEquals(100.5, point1.value());

        MacroPoint point2 = result.stream()
                .filter(p -> p.date().equals(LocalDate.of(2022, 1, 1)))
                .findFirst()
                .orElseThrow();
        assertEquals(200.75, point2.value());
    }

    @Test
    void shouldThrowScrapeException_whenInvalidJson() {
        // given
        String invalidJson = "{ invalid json }";

        // when && then
        assertThrows(ScrapeException.class, () -> parser.parse(invalidJson));
    }

    @Test
    void shouldReturnEmptyStream_whenNoTimeLabels() throws ScrapeException {
        // given
        String jsonNoTime = """
                {
                  "value": {
                    "0": 100.5
                  },
                  "dimension": {}
                }
                """;

        // when
        List<MacroPoint> result = parser.parse(jsonNoTime).toList();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyStream_whenValueFieldIsMissing() throws ScrapeException {
        // given
        String jsonNoValue = """
                {
                  "version": "2.0",
                  "class": "dataset",
                  "dimension": {
                    "time": {
                      "category": {
                        "index": { "2021": 0 }
                      }
                    }
                  }
                }
                """;

        // when
        List<MacroPoint> result = parser.parse(jsonNoValue).toList();

        // then
        assertTrue(result.isEmpty());
    }
}
