package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.domain.scb.GeoVersion;
import com.kg.macroanalyzer.application.domain.scb.ScbAreaValue;
import com.kg.macroanalyzer.application.domain.scb.ScbAvailability;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicator;
import com.kg.macroanalyzer.application.domain.scb.ScbIndicatorResult;
import com.kg.macroanalyzer.application.domain.scb.ScbLevel;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class ScbRegionalRepositoryTest {

    private static final Instant UPDATED = Instant.parse("2026-03-24T07:00:00Z");

    @Autowired
    ScbRegionalRepository repository;
    @Autowired
    DatabasePort databasePort;

    @Test
    void shouldSeedTheConfigRow_andParseEveryIndicator() {
        // when
        final var config = databasePort.readSeriesConfigList().stream()
                .filter(c -> c.name().equals("ScbRegionalStatistics"))
                .findFirst();
        final var indicators = repository.readIndicators();

        // then
        assertEquals("https://statistikdatabasen.scb.se/api/v2", config.map(SeriesConfig::scrapeUrl).orElseThrow());
        assertEquals("SCB", config.map(SeriesConfig::category).orElseThrow());
        assertEquals(26, indicators.size());
        final var share65 = indicators.stream().filter(i -> i.id().equals("share_65_plus")).findFirst().orElseThrow();
        assertEquals(List.of(ScbLevel.DESO, ScbLevel.REGSO), share65.levels());
        assertEquals(List.of("000007Y7"), share65.numerator().getFirst().selection().get("ContentsCode"));
        assertEquals(List.of("65-69", "70-74", "75-79", "80-"), share65.numerator().getFirst().selection().get("Alder"));
        assertEquals(List.of("totalt"), share65.denominator().getFirst().selection().get("Alder"));
        assertEquals(100.0, share65.multiplier());
        final var cars = indicators.stream().filter(i -> i.id().equals("cars_per_1000")).findFirst().orElseThrow();
        assertEquals(Set.of("TAB6091", "TAB6589", "TAB6574"), cars.tables());
        final var migration = indicators.stream().filter(i -> i.id().equals("net_migration")).findFirst().orElseThrow();
        assertEquals(List.of("000004V8", "000004V9"), migration.numerator().getFirst().selection().get("ContentsCode"));
        assertEquals(List.of(ScbLevel.REGSO), migration.levels());
        indicators.forEach(i -> assertFalse(i.numerator().isEmpty(), i.id()));
    }

    @Test
    void shouldUpsertStamps_andDropTheGivenTables() {
        // when
        repository.writeTableStamps(Map.of("TAB0001", Instant.parse("2025-01-01T00:00:00Z"), "TAB0002", UPDATED), Set.of());
        repository.writeTableStamps(Map.of("TAB0001", UPDATED), Set.of("TAB0002"));

        // then
        final var stamps = repository.readTableStamps();
        assertEquals(UPDATED, stamps.get("TAB0001"));
        assertFalse(stamps.containsKey("TAB0002"));
    }

    @Test
    void shouldReplaceTheIndicator_whenWrittenTwice() {
        // given
        final var indicator = indicator("households");
        final var many = IntStream.range(0, 12_000)
                .mapToObj(i -> new ScbAreaValue(2023, GeoVersion.DESO_2018, "A%05d".formatted(i), i))
                .toList();
        final var replacement = List.of(
                new ScbAreaValue(2024, GeoVersion.DESO_2025, "0114A0010", 12.5),
                new ScbAreaValue(2024, GeoVersion.REGSO_2025, "0114R001", 0.0)
        );

        // when
        repository.writeIndicatorResult(result(indicator, many, Map.of("TAB6568", Instant.parse("2025-01-01T00:00:00Z"))));
        final var firstAvailability = repository.readAvailability("households");
        repository.writeIndicatorResult(result(indicator, replacement, Map.of("TAB6568", UPDATED)));

        // then
        assertEquals(List.of(new ScbAvailability("households", GeoVersion.DESO_2018, 2023)), firstAvailability);
        assertEquals(
                Set.of(
                        new ScbAvailability("households", GeoVersion.DESO_2025, 2024),
                        new ScbAvailability("households", GeoVersion.REGSO_2025, 2024)
                ),
                new HashSet<>(repository.readAvailability("households"))
        );
        assertTrue(repository.readAvailability().containsAll(repository.readAvailability("households")));
        assertEquals(Map.of("0114A0010", 12.5), repository.readValues("households", GeoVersion.DESO_2025, 2024));
        assertEquals(Map.of("0114R001", 0.0), repository.readValues("households", GeoVersion.REGSO_2025, 2024));
        assertEquals(Map.of(), repository.readValues("households", GeoVersion.DESO_2018, 2023));
        // Stamps are written separately, once every indicator of the run is stored
        assertFalse(repository.readTableStamps().containsKey("TAB6568"));
        assertTrue(repository.readIndicatorsWithData().contains("households"));
        assertFalse(repository.readIndicatorsWithData().contains("turnout_region"));
    }

    private ScbIndicator indicator(String id) {
        return repository.readIndicators().stream()
                .filter(i -> i.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private static ScbIndicatorResult result(
            ScbIndicator indicator,
            List<ScbAreaValue> values,
            Map<String, Instant> stamps
    ) {
        return ScbIndicatorResult.builder()
                .indicator(indicator)
                .values(values)
                .tableStamps(stamps)
                .build();
    }

}
