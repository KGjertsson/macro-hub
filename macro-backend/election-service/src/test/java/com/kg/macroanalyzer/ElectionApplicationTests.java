package com.kg.macroanalyzer;

import com.kg.macroanalyzer.election.domain.ElectionScrapeFlows;
import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.kg.macroanalyzer.election.jooq.generated.Tables.LOCAL_ELECTION_AREA;
import static com.kg.macroanalyzer.election.jooq.generated.Tables.LOCAL_ELECTION_PARTY_RESULT;
import static com.kg.macroanalyzer.election.jooq.generated.Tables.LOCAL_GOVERNANCE;
import static com.kg.macroanalyzer.platform.jooq.generated.Tables.SERIES_CONFIGURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Starts the whole application on an empty database, which runs the platform's migrations and then the service's. */
@SpringBootTest
@ActiveProfiles("test")
class ElectionApplicationTests {

    @Autowired
    DSLContext dslContext;
    @Autowired
    SeriesConfigPort seriesConfigPort;

    @Test
    void shouldHaveRunThePlatformMigrations_inTheirOwnHistoryTable() {
        assertEquals(List.of("1"), successfulVersions("flyway_platform_schema_history"));
    }

    @Test
    void shouldHaveAppliedTheServiceMigrations_onTopOfABaselineAtVersionZero() {
        final var versions = successfulVersions("flyway_schema_history");

        assertEquals("0", versions.getFirst());
        assertTrue(versions.contains("1"), versions.toString());
    }

    @Test
    void shouldRouteEveryConfig_toItsFlow() {
        // when
        final var flowByName = seriesConfigPort.readSeriesConfigList().stream()
                .collect(Collectors.toMap(SeriesConfig::name, SeriesConfig::scrapeFlow));

        // then
        assertEquals(Map.of(
                "MunicipalElections", ElectionScrapeFlows.LOCAL_ELECTION,
                "RegionElections", ElectionScrapeFlows.LOCAL_ELECTION,
                "ParliamentElections", ElectionScrapeFlows.LOCAL_ELECTION
        ), flowByName);
    }

    @Test
    void shouldKeepTheConfigs_inTheirIdOrder() {
        // when
        final var names = seriesConfigPort.readSeriesConfigList().stream().map(SeriesConfig::name).toList();

        // then
        assertEquals(List.of("MunicipalElections", "RegionElections", "ParliamentElections"), names);
    }

    @Test
    void shouldLeaveTheChartMetadataEmpty() {
        // when
        final var municipalElections = seriesConfigPort.readSeriesConfigList().stream()
                .filter(c -> c.name().equals("MunicipalElections"))
                .findFirst()
                .orElseThrow();

        // then
        assertEquals("Valmyndigheten", municipalElections.category());
        assertNull(municipalElections.unit());
        assertNull(municipalElections.subcategory());
        assertNull(municipalElections.country());
        assertNull(municipalElections.maturityMonths());
    }

    @Test
    void shouldGiveNewConfigsHigherIds_thanTheCopiedOnes() {
        // when
        final var newId = dslContext.insertInto(SERIES_CONFIGURATION)
                .set(SERIES_CONFIGURATION.GLOBAL_ID, UUID.randomUUID())
                .set(SERIES_CONFIGURATION.NAME, "New")
                .set(SERIES_CONFIGURATION.DISPLAY_NAME, "New")
                .set(SERIES_CONFIGURATION.SCRAPE_FLOW, "NEW")
                .returning(SERIES_CONFIGURATION.ID)
                .fetchSingle(SERIES_CONFIGURATION.ID);
        dslContext.deleteFrom(SERIES_CONFIGURATION).where(SERIES_CONFIGURATION.ID.eq(newId)).execute();

        // then
        assertTrue(newId > 115, () -> "new id " + newId);
    }

    // A scrape fills the results; who governs has no open data source and only migrations add rows
    @Test
    void shouldStartWithEmptyTables() {
        assertEquals(0, dslContext.fetchCount(LOCAL_ELECTION_AREA));
        assertEquals(0, dslContext.fetchCount(LOCAL_ELECTION_PARTY_RESULT));
        assertEquals(0, dslContext.fetchCount(LOCAL_GOVERNANCE));
    }

    private List<String> successfulVersions(String historyTable) {
        return dslContext.fetch(
                "select version from %s where success order by installed_rank".formatted(historyTable)
        ).map(r -> r.get(0, String.class));
    }

}
