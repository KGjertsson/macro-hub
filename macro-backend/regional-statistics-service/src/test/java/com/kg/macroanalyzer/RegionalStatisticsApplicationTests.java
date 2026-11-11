package com.kg.macroanalyzer;

import com.kg.macroanalyzer.regionalstatistics.domain.RegionalStatisticsScrapeFlows;
import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
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
class RegionalStatisticsApplicationTests {

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
    void shouldHoldOnlyThisServicesConfig() {
        // when
        final var configs = seriesConfigPort.readSeriesConfigList();

        // then
        assertEquals(1, configs.size());
        assertEquals("ScbRegionalStatistics", configs.getFirst().name());
        assertEquals(RegionalStatisticsScrapeFlows.SCB_REGIONAL, configs.getFirst().scrapeFlow());
    }

    @Test
    void shouldKeepTheCopiedConfigIds_andGiveNewRowsHigherOnes() {
        // given
        final var copied = dslContext.selectFrom(SERIES_CONFIGURATION).fetchSingle();

        // when
        final var newId = dslContext.insertInto(SERIES_CONFIGURATION)
                .set(SERIES_CONFIGURATION.GLOBAL_ID, UUID.randomUUID())
                .set(SERIES_CONFIGURATION.NAME, "New")
                .set(SERIES_CONFIGURATION.DISPLAY_NAME, "New")
                .set(SERIES_CONFIGURATION.SCRAPE_FLOW, "NEW")
                .returning(SERIES_CONFIGURATION.ID)
                .fetchSingle(SERIES_CONFIGURATION.ID);
        dslContext.deleteFrom(SERIES_CONFIGURATION)
                .where(SERIES_CONFIGURATION.ID.eq(newId))
                .execute();

        // then
        assertEquals(116, copied.getId());
        assertEquals(
                UUID.fromString("1644ab25-28f8-4ef6-910f-ff69bf4469d2"),
                copied.getGlobalId()
        );
        assertTrue(newId > copied.getId(), () -> "new id " + newId);
    }

    private List<String> successfulVersions(String historyTable) {
        return dslContext.fetch(
                "select version from %s where success order by installed_rank"
                        .formatted(historyTable)
        ).map(r -> r.get(0, String.class));
    }

}
