package com.kg.macroanalyzer.regionalstatistics.adaptors.web;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScbResponseParserTest {

    private final TestJsonReader testJsonReader = new TestJsonReader();

    @Test
    void shouldReadUpdatedStampAndCodesInOrder_fromMetadata() throws ScrapeException {
        // given
        final var response = testJsonReader.readResource("json/scb/metadata_sample.json");

        // when
        final var metadata = new ScbMetadataResponseParser("TAB6574").parse(response);

        // then
        assertEquals("TAB6574", metadata.table());
        assertEquals(Instant.parse("2026-03-24T07:00:00Z"), metadata.updated());
        assertEquals(List.of("Region", "Alder", "ContentsCode", "Tid"), List.copyOf(metadata.codes().keySet()));
        assertEquals(
                List.of("0114", "0114A0010", "0114A0020", "0114A0010_DeSO2025", "0114A0020_DeSO2025", "0114R001"),
                metadata.regionCodes()
        );
        assertEquals(List.of("totalt", "65-69", "70-74"), metadata.codes().get("Alder"));
        assertEquals(List.of("2023", "2024"), metadata.years());
    }

    @Test
    void shouldSumOtherDimensionsPerRegionAndYear_withNullWinning() throws ScrapeException {
        // given
        final var response = testJsonReader.readResource("json/scb/data_sample.json");

        // when
        final var values = new ScbDataResponseParser("TAB6574").parse(response).values();

        // then
        assertEquals(Map.of(2023, 800.0, 2024, 820.0), values.get("0114"));
        assertEquals(Map.of(2023, 150.0, 2024, 0.0), values.get("0114A0010"));
        assertEquals(yearValues(2023, 0.0, 2024, null), values.get("0114A0010_DeSO2025"));
        assertEquals(yearValues(2023, null, 2024, 11.0), values.get("0114R001"));
        assertEquals(6, values.size());
    }

    @Test
    void shouldReadSparseValueObjects() throws ScrapeException {
        // given
        final var response = """
                {"id": ["Region", "Tid"], "size": [2, 2],
                 "dimension": {"Region": {"category": {"index": ["0114R001", "0114R002"]}},
                               "Tid": {"category": {"index": {"2022": 0, "2023": 1}}}},
                 "value": {"0": 1.5, "3": 2}}
                """;

        // when
        final var values = new ScbDataResponseParser("TAB5750").parse(response).values();

        // then
        assertEquals(yearValues(2022, 1.5, 2023, null), values.get("0114R001"));
        assertEquals(yearValues(2022, null, 2023, 2.0), values.get("0114R002"));
    }

    @Test
    void shouldFail_whenResponseIsAnErrorOrEmpty() {
        // given
        final var parser = new ScbDataResponseParser("TAB6574");

        // then
        assertThrows(ScrapeException.class, () -> parser.parse(""));
        assertThrows(ScrapeException.class, () -> parser.parse("{\"title\":\"Non-existent table\",\"status\":404}"));
        assertThrows(ScrapeException.class, () -> new ScbMetadataResponseParser("TAB6574").parse("not json"));
    }

    // Map.of does not allow the null that marks a missing cell
    private static Map<Integer, Double> yearValues(int firstYear, Double first, int secondYear, Double second) {
        final var values = new HashMap<Integer, Double>();
        values.put(firstYear, first);
        values.put(secondYear, second);

        return values;
    }

}
