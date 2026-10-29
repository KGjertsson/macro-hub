package com.kg.macroanalyzer.application.domain.scb;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.ScbDataResponseParser;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScbValuesTest {

    private static final ScbSlice DESO_2018_2023 = new ScbSlice(2023, GeoVersion.DESO_2018);
    private static final ScbSlice DESO_2025_2024 = new ScbSlice(2024, GeoVersion.DESO_2025);
    private static final ScbSlice REGSO_2020_2024 = new ScbSlice(2024, GeoVersion.REGSO_2020);

    @Test
    void shouldSplitFamilies_keepOnlyYearsWithData_andStripTheSuffix() throws ScrapeException {
        // given
        final var response = new TestJsonReader().readResource("json/scb/data_sample.json");
        final var values = new ScbDataResponseParser("TAB6574").parse(response);

        // when
        final var slices = ScbValues.slices(values);

        // then
        // DeSO 2018 has data in 2023 only; a real 0 in a kept year stays
        assertEquals(Map.of("0114A0010", 150.0, "0114A0020", 0.0), slices.get(DESO_2018_2023));
        // DeSO 2025 is all 0 in 2023, and its null in 2024 is dropped
        assertEquals(Map.of("0114A0020", 40.0), slices.get(DESO_2025_2024));
        // RegSO 2020 has only a null in 2023
        assertEquals(Map.of("0114R001", 11.0), slices.get(REGSO_2020_2024));
        // The kommun code 0114 belongs to no version
        assertEquals(Set.of(DESO_2018_2023, DESO_2025_2024, REGSO_2020_2024), slices.keySet());
    }

    @Test
    void shouldRecogniseEachVersion_fromTheRegionCode() {
        assertEquals(GeoVersion.DESO_2018, GeoVersion.fromRegionCode("0180C1010").orElseThrow());
        assertEquals(GeoVersion.DESO_2025, GeoVersion.fromRegionCode("0180C1010_DeSO2025").orElseThrow());
        assertEquals(GeoVersion.REGSO_2020, GeoVersion.fromRegionCode("0180R001").orElseThrow());
        assertEquals(GeoVersion.REGSO_2025, GeoVersion.fromRegionCode("0180R001_RegSO2025").orElseThrow());
        assertTrue(GeoVersion.fromRegionCode("0180").isEmpty());
        assertTrue(GeoVersion.fromRegionCode("01").isEmpty());
        assertEquals("0180R001", GeoVersion.REGSO_2025.areaCode("0180R001_RegSO2025"));
        assertEquals("0180C1010", GeoVersion.DESO_2018.areaCode("0180C1010"));
        assertTrue(GeoVersion.DESO_2018.matchesWildcard("0180C1010"));
        assertTrue(GeoVersion.REGSO_2025.matchesWildcard("0180R001_RegSO2025"));
        assertEquals(List.of(GeoVersion.REGSO_2020, GeoVersion.REGSO_2025), ScbLevel.REGSO.versions());
    }

    @Test
    void shouldDivideOnMatchingAreas_andSkipMissingOrZeroDenominators() {
        // given
        final var numerator = Map.of(
                DESO_2018_2023, Map.of("A", 25.0, "B", 1.0, "C", 3.0),
                DESO_2025_2024, Map.of("A", 1.0)
        );
        final var denominator = Map.of(
                DESO_2018_2023, Map.of("A", 200.0, "B", 0.0)
        );

        // when
        final var ratio = ScbValues.ratio(numerator, denominator, 100);

        // then
        assertEquals(Map.of(DESO_2018_2023, Map.of("A", 12.5)), ratio);
    }

    @Test
    void shouldRoundRatios_toFourDecimals() {
        // when
        final var ratio = ScbValues.ratio(
                Map.of(DESO_2018_2023, Map.of("A", 1.0)),
                Map.of(DESO_2018_2023, Map.of("A", 3.0)),
                100
        );

        // then
        assertEquals(33.3333, ratio.get(DESO_2018_2023).get("A"));
    }

    @Test
    void shouldScale_withoutDenominator() {
        // when
        final var scaled = ScbValues.scale(Map.of(REGSO_2020_2024, Map.of("A", 2.5)), 1);

        // then
        assertEquals(Map.of(REGSO_2020_2024, Map.of("A", 2.5)), scaled);
        assertEquals(
                List.of(new ScbAreaValue(2024, GeoVersion.REGSO_2020, "A", 2.5)),
                ScbValues.toAreaValues(scaled)
        );
    }

    @Test
    void shouldChunkYears_toStayWithinTheCellLimit() {
        // given
        final var years = List.of("2019", "2020", "2021", "2022", "2023", "2024", "2025");

        // then
        // 6 160 DeSO areas x 4 age groups = 24 640 cells per year, so 6 years fit in 150 000 cells
        assertEquals(
                List.of(List.of("2019", "2020", "2021", "2022", "2023", "2024"), List.of("2025")),
                ScbValues.yearChunks(years, 6_160 * 4, 150_000)
        );
        assertEquals(List.of(years), ScbValues.yearChunks(years, 3_363, 150_000));
        // A year is never split, even when it alone exceeds the limit
        assertEquals(7, ScbValues.yearChunks(years, 200_000, 150_000).size());
    }

}
