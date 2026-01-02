package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responseparser;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.FedResponseParser;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FedResponseParserTest {

    private FedResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new FedResponseParser();
    }

    @Test
    void shouldParseValidResponseCorrectly() throws ScrapeException {
        // given
        String jsonResponse = """
                {
                  "realtime_start": "2023-01-01",
                  "realtime_end": "2023-01-01",
                  "observation_start": "2021-01-01",
                  "observation_end": "2021-01-02",
                  "units": "lin",
                  "output_type": 1,
                  "file_type": "json",
                  "order_by": "observation_date",
                  "sort_order": "asc",
                  "count": 2,
                  "offset": 0,
                  "limit": 100000,
                  "observations": [
                    {
                      "realtime_start": "2023-01-01",
                      "realtime_end": "2023-01-01",
                      "date": "2021-01-01",
                      "value": "100.5"
                    },
                    {
                      "realtime_start": "2023-01-01",
                      "realtime_end": "2023-01-01",
                      "date": "2021-01-02",
                      "value": "200.75"
                    }
                  ]
                }
                """;

        // when
        List<MacroPoint> result = parser.parse(jsonResponse).toList();

        // then
        assertEquals(2, result.size());

        MacroPoint point1 = result.getFirst();
        assertEquals(LocalDate.of(2021, 1, 1), point1.date());
        assertEquals(100.5, point1.value());

        MacroPoint point2 = result.get(1);
        assertEquals(LocalDate.of(2021, 1, 2), point2.date());
        assertEquals(200.75, point2.value());
    }

    @Test
    void shouldSkipObservationsWithInvalidValues() throws ScrapeException {
        // given
        String jsonResponse = """
                {
                  "observations": [
                    {
                      "date": "2021-01-01",
                      "value": "100.5"
                    },
                    {
                      "date": "2021-01-02",
                      "value": "."
                    }
                  ]
                }
                """;

        // when
        List<MacroPoint> result = parser.parse(jsonResponse).toList();

        // then
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2021, 1, 1), result.getFirst().date());
        assertEquals(100.5, result.getFirst().value());
    }

    @Test
    void shouldThrowScrapeException_whenInvalidJson() {
        // given
        String invalidJson = "{ invalid json }";

        // when && then
        assertThrows(ScrapeException.class, () -> parser.parse(invalidJson));
    }
}
