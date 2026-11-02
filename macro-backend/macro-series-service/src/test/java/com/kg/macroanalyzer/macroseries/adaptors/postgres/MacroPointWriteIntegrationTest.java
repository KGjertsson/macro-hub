package com.kg.macroanalyzer.macroseries.adaptors.postgres;

import com.kg.macroanalyzer.macroseries.application.port.in.MacroSeriesQueries;
import com.kg.macroanalyzer.macroseries.application.port.out.ConfigWithMacroPoints;
import com.kg.macroanalyzer.macroseries.application.port.out.MacroSeriesRepositoryPort;
import com.kg.macroanalyzer.macroseries.application.service.scrape.ExistingMacroPointFinder;
import com.kg.macroanalyzer.macroseries.application.service.scrape.MacroPointPersister;
import com.kg.macroanalyzer.macroseries.application.service.scrape.MacroPointScraper;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.macroseries.jooq.generated.Public;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.kg.macroanalyzer.macroseries.jooq.generated.Tables.FED_SERIES;
import static com.kg.macroanalyzer.macroseries.jooq.generated.Tables.INT_GOV_BOND_10_YEAR_USA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class MacroPointWriteIntegrationTest {

    private static final LocalDate DAY_1 = LocalDate.of(2020, 1, 1);
    private static final LocalDate DAY_2 = LocalDate.of(2020, 1, 2);
    private static final String FED_NAME = "FEDPublicDebtAsGDPPercentage";

    @Autowired
    MacroSeriesRepositoryPort repositoryPort;
    @Autowired
    MacroSeriesQueries macroSeriesQueries;
    @Autowired
    DSLContext dslContext;

    @AfterEach
    void removeWrittenPoints() {
        Public.PUBLIC.getTables().forEach(table -> dslContext.deleteFrom(table).execute());
    }

    // All 41 tables, through the same names the scrape flow uses
    @Test
    void shouldReplaceTheRevisedValueAndKeepTheOthers_forEverySeries() {
        macroSeriesQueries.getChartSeriesConfigList().forEach(config -> {
            // given
            write(config, point(DAY_1, 1.0), point(DAY_2, 2.0));

            // when
            final var changed = write(config, point(DAY_1, 3.0));

            // then
            assertEquals(1, changed, config.name());
            assertEquals(
                    List.of(point(DAY_1, 3.0), point(DAY_2, 2.0)),
                    repositoryPort.readMacroSeries(config).macroPoints().stream().sorted().toList(),
                    config.name()
            );
        });
    }

    @Test
    void shouldKeepTheSameDate_forDifferentSeriesInOneTable() {
        // given
        final var debt = config(FED_NAME);
        final var surplus = config("FEDSurplusOrDeficit");

        // when
        write(debt, point(DAY_1, 1.0));
        write(surplus, point(DAY_1, 2.0));
        write(debt, point(DAY_1, 3.0));

        // then
        assertEquals(List.of(point(DAY_1, 3.0)), repositoryPort.readMacroSeries(debt).macroPoints());
        assertEquals(List.of(point(DAY_1, 2.0)), repositoryPort.readMacroSeries(surplus).macroPoints());
    }

    @Test
    void shouldUpdateTheOneRow_whenAScrapeFindsARevisedValue() throws Exception {
        // given
        final var config = config(FED_NAME);
        scrape(config, point(DAY_1, 1.0), point(DAY_2, 2.0));
        final var before = fedRows();

        // when
        final var result = scrape(config, point(DAY_1, 9.0), point(DAY_2, 2.0));

        // then
        final var after = fedRows();
        assertEquals(ScrapeResult.SUCCESS, result);
        assertEquals(2, after.size());
        final var revisedBefore = before.getFirst();
        final var revisedAfter = after.getFirst();
        assertEquals(9.0, revisedAfter.getValue(FED_SERIES.VALUE));
        assertEquals(revisedBefore.getValue(FED_SERIES.ID), revisedAfter.getValue(FED_SERIES.ID));
        assertEquals(revisedBefore.getValue(FED_SERIES.GLOBAL_ID), revisedAfter.getValue(FED_SERIES.GLOBAL_ID));
        assertEquals(revisedBefore.getValue(FED_SERIES.CREATED), revisedAfter.getValue(FED_SERIES.CREATED));
        assertTrue(revisedAfter.getValue(FED_SERIES.UPDATED).isAfter(revisedBefore.getValue(FED_SERIES.UPDATED)));
        assertEquals(before.getLast(), after.getLast());
    }

    @Test
    void shouldAddAndRewriteNothing_whenTheSameScrapeRunsAgain() throws Exception {
        // given
        final var config = config(FED_NAME);
        scrape(config, point(DAY_1, 1.0), point(DAY_2, 2.0));
        final var before = fedRows();

        // when
        final var result = scrape(config, point(DAY_1, 1.0), point(DAY_2, 2.0));

        // then
        assertEquals(ScrapeResult.EMPTY, result);
        assertEquals(before, fedRows());
    }

    @Test
    void shouldGoBackToTheEarlierValue_whenARevisionIsUndone() throws Exception {
        // given
        final var config = config(FED_NAME);
        scrape(config, point(DAY_1, 1.0));
        scrape(config, point(DAY_1, 2.0));

        // when
        scrape(config, point(DAY_1, 1.0));

        // then
        assertEquals(List.of(point(DAY_1, 1.0)), repositoryPort.readMacroSeries(config).macroPoints());
    }

    @Test
    void shouldNotRewriteTheRow_whenTheSamePointIsWrittenAgainPastTheScrapersFilter() {
        // given
        final var config = config(FED_NAME);
        write(config, point(DAY_1, 1.0));
        final var before = fedRows();

        // when
        final var changed = write(config, point(DAY_1, 1.0));

        // then
        assertEquals(0, changed);
        assertEquals(before, fedRows());
    }

    @Test
    void shouldStoreOneRow_whenAScrapeHoldsADateTwice() {
        // given
        final var config = config(FED_NAME);

        // when
        write(config, point(DAY_1, 1.0), point(DAY_1, 2.0));

        // then
        assertEquals(List.of(point(DAY_1, 2.0)), repositoryPort.readMacroSeries(config).macroPoints());
    }

    @Test
    void shouldStoreOneRowPerDate_whenWritesOfTheSamePointsRace() throws Exception {
        // given
        final var fed = config(FED_NAME);
        final var bond = config("IntGovBondsUsa10Year");
        final var points = IntStream.range(0, 300)
                .mapToObj(i -> point(DAY_1.plusDays(i), (double) i))
                .toList();

        // when
        race(8, ignored -> {
            repositoryPort.writeMacroPoints(new ConfigWithMacroPoints(fed, points));
            repositoryPort.writeMacroPoints(new ConfigWithMacroPoints(bond, points));
        });

        // then
        assertEquals(points, repositoryPort.readMacroSeries(fed).macroPoints().stream().sorted().toList());
        assertEquals(points, repositoryPort.readMacroSeries(bond).macroPoints().stream().sorted().toList());
    }

    @Test
    void shouldStoreOneRowPerDate_whenRacingWritesDisagreeOnTheValue() throws Exception {
        // given
        final var config = config(FED_NAME);
        final var dates = IntStream.range(0, 100).mapToObj(DAY_1::plusDays).toList();

        // when
        race(8, writer -> repositoryPort.writeMacroPoints(new ConfigWithMacroPoints(
                config,
                dates.stream().map(date -> point(date, (double) writer)).toList()
        )));

        // then
        final var stored = repositoryPort.readMacroSeries(config).macroPoints();
        assertEquals(dates.size(), stored.size());
        assertEquals(dates, stored.stream().sorted().map(p -> (LocalDate) p.date()).toList());
    }

    @Test
    void shouldRejectASecondRow_forASeriesAndDate() {
        // given
        write(config(FED_NAME), point(DAY_1, 1.0));
        write(config("IntGovBondsUsa10Year"), point(DAY_1, 1.0));

        // then
        assertThrows(DataAccessException.class, () -> dslContext.insertInto(
                FED_SERIES, FED_SERIES.GLOBAL_ID, FED_SERIES.SERIES_NAME, FED_SERIES.VALUE, FED_SERIES.DATE
        ).values(UUID.randomUUID(), FED_NAME, 2.0, DAY_1).execute());
        assertThrows(DataAccessException.class, () -> dslContext.insertInto(
                INT_GOV_BOND_10_YEAR_USA,
                INT_GOV_BOND_10_YEAR_USA.GLOBAL_ID,
                INT_GOV_BOND_10_YEAR_USA.VALUE,
                INT_GOV_BOND_10_YEAR_USA.DATE
        ).values(UUID.randomUUID(), 2.0, DAY_1).execute());
    }

    private int write(SeriesConfig config, MacroPoint... points) {
        return repositoryPort.writeMacroPoints(new ConfigWithMacroPoints(config, List.of(points)));
    }

    // The scrape flow's own steps, with the web stubbed
    private ScrapeResult scrape(SeriesConfig config, MacroPoint... scraped) throws Exception {
        final var scraper = new MacroPointScraper(ignored -> Stream.of(scraped));
        final var persister = new MacroPointPersister(repositoryPort, Mockito.mock(ScrapeQueuePort.class));
        final var existing = new ExistingMacroPointFinder(repositoryPort).findExisting(config).orElseThrow();

        return persister.persist(scraper.scrape(existing).orElseThrow());
    }

    private void race(int writers, Writer writer) throws Exception {
        final var start = new CountDownLatch(1);
        try (final var executor = Executors.newFixedThreadPool(writers)) {
            final List<Future<?>> futures = IntStream.range(0, writers)
                    .<Future<?>>mapToObj(i -> executor.submit(() -> {
                        start.await();
                        writer.write(i);
                        return null;
                    }))
                    .toList();
            start.countDown();
            for (final var future : futures) {
                future.get();
            }
        }
    }

    private org.jooq.Result<?> fedRows() {
        return dslContext.selectFrom(FED_SERIES).orderBy(FED_SERIES.DATE).fetch();
    }

    private SeriesConfig config(String name) {
        return macroSeriesQueries.getChartSeriesConfigList().stream()
                .filter(c -> c.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static MacroPoint point(LocalDate date, Double value) {
        return MacroPoint.builder().date(date).value(value).build();
    }

    @FunctionalInterface
    private interface Writer {
        void write(int writer);
    }

}
