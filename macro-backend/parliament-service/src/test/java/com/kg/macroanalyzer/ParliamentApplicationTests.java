package com.kg.macroanalyzer;

import com.kg.macroanalyzer.parliament.domain.ParliamentScrapeFlows;
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

import static com.kg.macroanalyzer.parliament.jooq.generated.Tables.RIKSDAG_ELECTION;
import static com.kg.macroanalyzer.parliament.jooq.generated.Tables.RIKSDAG_ELECTION_RESULT;
import static com.kg.macroanalyzer.parliament.jooq.generated.Tables.RIKSDAG_ORGAN;
import static com.kg.macroanalyzer.parliament.jooq.generated.Tables.RIKSDAG_PARTY;
import static com.kg.macroanalyzer.platform.jooq.generated.Tables.SERIES_CONFIGURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ParliamentApplicationTests {

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
                "MembersOfParliament", ParliamentScrapeFlows.MEMBER_OF_PARLIAMENT,
                "MembersOfParliamentActivity", ParliamentScrapeFlows.MEMBER_ACTIVITY
        ), flowByName);
    }

    @Test
    void shouldLeaveTheChartMetadataEmpty() {
        // when
        final var membersOfParliament = seriesConfigPort.readSeriesConfigList().stream()
                .filter(c -> c.name().equals("MembersOfParliament"))
                .findFirst()
                .orElseThrow();

        // then
        assertEquals("Riksdagen", membersOfParliament.category());
        assertNull(membersOfParliament.unit());
        assertNull(membersOfParliament.subcategory());
        assertNull(membersOfParliament.country());
        assertNull(membersOfParliament.maturityMonths());
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
        assertTrue(newId > 112, () -> "new id " + newId);
    }

    // Only migrations write these, and no scrape can bring them back
    @Test
    void shouldSeedTheCuratedTables() {
        assertEquals(11, dslContext.fetchCount(RIKSDAG_PARTY));
        assertEquals(11, dslContext.fetchCount(RIKSDAG_ELECTION));
        assertEquals(81, dslContext.fetchCount(RIKSDAG_ELECTION_RESULT));
        assertEquals(43, dslContext.fetchCount(RIKSDAG_ORGAN));
    }

    // /parliament/parties answers in the table's order, which the update of SD's colour left with SD last
    @Test
    void shouldSeedThePartiesWithSdLast_andSdsColour() {
        // when
        final var codes = dslContext.select(RIKSDAG_PARTY.CODE).from(RIKSDAG_PARTY).fetch(RIKSDAG_PARTY.CODE);
        final var sdColor = dslContext.select(RIKSDAG_PARTY.COLOR)
                .from(RIKSDAG_PARTY)
                .where(RIKSDAG_PARTY.CODE.eq("SD"))
                .fetchSingle(RIKSDAG_PARTY.COLOR);

        // then
        assertEquals(List.of("S", "M", "C", "V", "KD", "L", "MP", "FP", "NYD", "-", "SD"), codes);
        assertEquals("#FFCC00", sdColor);
    }

    private List<String> successfulVersions(String historyTable) {
        return dslContext.fetch(
                "select version from %s where success order by installed_rank".formatted(historyTable)
        ).map(r -> r.get(0, String.class));
    }

}
