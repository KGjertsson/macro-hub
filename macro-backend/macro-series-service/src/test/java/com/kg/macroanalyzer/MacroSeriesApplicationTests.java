package com.kg.macroanalyzer;

import com.kg.macroanalyzer.macroseries.domain.MacroSeriesScrapeFlows;
import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static com.kg.macroanalyzer.platform.jooq.generated.Tables.SERIES_CONFIGURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class MacroSeriesApplicationTests {

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
    void shouldHoldOnlyThisServicesConfigs() {
        // when
        final var configs = seriesConfigPort.readSeriesConfigList();

        // then
        assertEquals(108, configs.size());
        configs.forEach(c -> assertEquals(MacroSeriesScrapeFlows.MACRO_POINT, c.scrapeFlow(), c.name()));
    }

    // /series-config lists the configs in id order, and /chart-data picks a series' colour from its place in that order
    @Test
    void shouldKeepTheCopiedConfigOrder_andIds() {
        // when
        final var names = seriesConfigPort.readSeriesConfigList().stream().map(SeriesConfig::name).toList();
        final var ids = dslContext.select(SERIES_CONFIGURATION.ID)
                .from(SERIES_CONFIGURATION)
                .orderBy(SERIES_CONFIGURATION.ID)
                .fetch(SERIES_CONFIGURATION.ID);

        // then
        assertEquals("PolicyRateSweden", names.getFirst());
        assertEquals("DebtPcGdpSk", names.getLast());
        assertEquals(1, ids.getFirst());
        assertEquals(110, ids.getLast());
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
        assertTrue(newId > 110, () -> "new id " + newId);
    }

    private List<String> successfulVersions(String historyTable) {
        return dslContext.fetch(
                "select version from %s where success order by installed_rank".formatted(historyTable)
        ).map(r -> r.get(0, String.class));
    }

}
