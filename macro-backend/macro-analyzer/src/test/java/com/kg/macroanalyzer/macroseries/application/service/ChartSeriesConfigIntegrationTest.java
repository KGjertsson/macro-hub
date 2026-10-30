package com.kg.macroanalyzer.macroseries.application.service;

import com.kg.macroanalyzer.macroseries.adaptors.http.SeriesConfigResponse;
import com.kg.macroanalyzer.macroseries.application.port.in.MacroSeriesQueries;
import com.kg.macroanalyzer.macroseries.domain.MacroSeriesScrapeFlows;
import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class ChartSeriesConfigIntegrationTest {

    @Autowired
    MacroSeriesQueries macroSeriesQueries;
    @Autowired
    SeriesConfigPort seriesConfigPort;
    @Autowired
    JsonMapper jsonMapper;

    @Test
    void shouldUseTheSwedishCategories_forEveryChartSeries() {
        // when
        final var categories = macroSeriesQueries.getChartSeriesConfigList().stream()
                .map(SeriesConfig::category)
                .collect(Collectors.toSet());

        // then
        assertEquals(Set.of("Räntor", "Statsobligationer", "Statsskuld", "Valuta", "Nyckeltal USA"), categories);
    }

    @Test
    void shouldHaveUnitAndSubcategory_forEveryChartSeries() {
        // when
        final var configs = macroSeriesQueries.getChartSeriesConfigList();

        // then
        assertEquals(108, configs.size());
        configs.forEach(c -> {
            assertEquals(MacroSeriesScrapeFlows.MACRO_POINT, c.scrapeFlow(), c.name());
            assertNotNull(c.unit(), c.name());
            assertNotNull(c.subcategory(), c.name());
        });
    }

    @Test
    void shouldRouteEveryNonChartConfig_toItsDomainsScrapeFlow() {
        // when
        final var flowByName = seriesConfigPort.readSeriesConfigList().stream()
                .filter(c -> !c.scrapeFlow().equals(MacroSeriesScrapeFlows.MACRO_POINT))
                .collect(Collectors.toMap(SeriesConfig::name, SeriesConfig::scrapeFlow));

        // then
        assertEquals(Map.of(
                "MembersOfParliament", "MEMBER_OF_PARLIAMENT",
                "MembersOfParliamentActivity", "MEMBER_ACTIVITY",
                "MunicipalElections", "LOCAL_ELECTION",
                "RegionElections", "LOCAL_ELECTION",
                "ParliamentElections", "LOCAL_ELECTION",
                "ScbRegionalStatistics", "SCB_REGIONAL"
        ), flowByName);
    }

    @Test
    void shouldHaveCountry_forEveryRateAndBond() {
        // when
        final var ratesAndBonds = macroSeriesQueries.getChartSeriesConfigList().stream()
                .filter(c -> Set.of("Räntor", "Statsobligationer").contains(c.category()))
                .toList();

        // then
        assertEquals(39, ratesAndBonds.size());
        ratesAndBonds.forEach(c -> assertNotNull(c.country(), c.name()));
    }

    @Test
    void shouldHaveMaturity_forEveryRateWithATerm() {
        // when
        final var termRates = macroSeriesQueries.getChartSeriesConfigList().stream()
                .filter(c -> Set.of("Euromarknadsränta", "Statsobligation", "Statsskuldväxel").contains(c.subcategory()))
                .toList();

        // then
        assertEquals(37, termRates.size());
        termRates.forEach(c -> assertNotNull(c.maturityMonths(), c.name()));
    }

    @Test
    void shouldSeparateNominalDebtFromDebtToGdp() {
        // when
        final var configs = seriesConfigPort.readSeriesConfigList();
        final var nominal = find(configs, "DebtMioEurAt");
        final var pctGdp = find(configs, "DebtPcGdpAt");

        // then
        assertEquals("Statsskuld", nominal.category());
        assertEquals("Nominell", nominal.subcategory());
        assertEquals("mn EUR", nominal.unit());
        assertEquals("Statsskuld", pctGdp.category());
        assertEquals("Andel av BNP", pctGdp.subcategory());
        assertEquals("% av BNP", pctGdp.unit());
        assertEquals("Österrike", pctGdp.country());
        assertEquals("Statsskuld i % av BNP, Österrike", pctGdp.displayName());
        assertNull(pctGdp.maturityMonths());
    }

    @Test
    void shouldListTheUsaDebt_withTheOtherNationalDebts() {
        // when
        final var chartSeries = macroSeriesQueries.getChartSeriesConfigList();
        final var nominal = find(chartSeries, "FEDTotalPublicDebt");
        final var pctGdp = find(chartSeries, "FEDPublicDebtAsGDPPercentage");
        final var deficit = find(chartSeries, "FEDSurplusOrDeficit");

        // then
        assertEquals("Statsskuld", nominal.category());
        assertEquals("Nominell", nominal.subcategory());
        assertEquals("mn USD", nominal.unit());
        assertEquals("USA", nominal.country());
        assertEquals("Statsskuld i miljoner dollar, USA", nominal.displayName());
        assertEquals("Statsskuld", pctGdp.category());
        assertEquals("Andel av BNP", pctGdp.subcategory());
        assertEquals("% av BNP", pctGdp.unit());
        assertEquals("USA", pctGdp.country());
        assertEquals("Statsskuld i % av BNP, USA", pctGdp.displayName());
        assertEquals("Nyckeltal USA", deficit.category());
        assertEquals(2 * 30, chartSeries.stream().filter(c -> c.category().equals("Statsskuld")).count());
    }

    @Test
    void shouldQuoteTheExchangeRate_inSekPerUsd() {
        // when
        final var usdSek = find(seriesConfigPort.readSeriesConfigList(), "UsdSekExchangeRate");

        // then
        assertEquals("Valuta", usdSek.category());
        assertEquals("SEK/USD", usdSek.unit());
    }

    @Test
    void shouldLeaveTheMetadataEmpty_forNonChartConfigs() {
        // when
        final var membersOfParliament = find(seriesConfigPort.readSeriesConfigList(), "MembersOfParliament");

        // then
        assertNull(membersOfParliament.unit());
        assertNull(membersOfParliament.subcategory());
        assertNull(membersOfParliament.country());
        assertNull(membersOfParliament.maturityMonths());
    }

    @Test
    void shouldSerializeTheMetadata_inCamelCase() {
        // given
        final var bond = find(seriesConfigPort.readSeriesConfigList(), "GovernmentBondSweden10Year");

        // when
        final var json = jsonMapper.writeValueAsString(SeriesConfigResponse.from(bond));

        // then
        assertFalse(json.contains("scrapeFlow"), json);
        assertTrue(json.contains("\"unit\":\"%\""), json);
        assertTrue(json.contains("\"subcategory\":\"Statsobligation\""), json);
        assertTrue(json.contains("\"country\":\"Sverige\""), json);
        assertTrue(json.contains("\"maturityMonths\":120"), json);
    }

    @Test
    void shouldHaveNoEmptyPathSegment_inAnyScrapeUrl() {
        // when
        final var configs = seriesConfigPort.readSeriesConfigList();

        // then
        configs.stream()
                .filter(c -> c.scrapeUrl() != null)
                .forEach(c -> assertFalse(c.scrapeUrl().contains("Observations//"), c.name()));
        assertEquals(
                "https://api.riksbank.se/swea/v1/Observations/EUDP3MGBP/1979-12-19",
                find(configs, "EuroMarketRateGB3Month").scrapeUrl()
        );
    }

    @Test
    void shouldScrapeTheSixMonthSeries_forJapanSixMonth() {
        // when
        final var japan6Month = find(seriesConfigPort.readSeriesConfigList(), "EuroMarketRateJapan6Month");

        // then
        assertEquals("https://api.riksbank.se/swea/v1/Observations/EUDP6MJPY/1979-11-29", japan6Month.scrapeUrl());
    }

    @Test
    void shouldNotHaveTheOldEuroAreaComposition() {
        // when
        final var names = seriesConfigPort.readSeriesConfigList().stream()
                .map(SeriesConfig::name)
                .toList();

        // then
        assertFalse(names.contains("DebtMioEurEa19"));
        assertFalse(names.contains("DebtPcGdpEa19"));
        assertTrue(names.contains("DebtMioEurEa20"));
        assertTrue(names.contains("DebtPcGdpEa20"));
    }

    @Test
    void shouldUseOneCountryLabel_forTheEuroArea() {
        // when
        final var euroArea = macroSeriesQueries.getChartSeriesConfigList().stream()
                .filter(c -> c.country() != null && c.country().startsWith("Euroområdet"))
                .toList();

        // then
        assertEquals(Set.of("Euroområdet"), euroArea.stream().map(SeriesConfig::country).collect(Collectors.toSet()));
        assertEquals(
                Set.of("Räntor", "Statsobligationer", "Statsskuld"),
                euroArea.stream().map(SeriesConfig::category).collect(Collectors.toSet())
        );
        assertEquals("Statsskuld i % av BNP, Euroområdet (EA20)", find(euroArea, "DebtPcGdpEa20").displayName());
    }

    @Test
    void shouldMatchTheCountry_toTheEurostatGeoCode() {
        // given
        final var countryByGeo = Map.ofEntries(
                Map.entry("at", "Österrike"),
                Map.entry("be", "Belgien"),
                Map.entry("bg", "Bulgarien"),
                Map.entry("cy", "Cypern"),
                Map.entry("cz", "Tjeckien"),
                Map.entry("de", "Tyskland"),
                Map.entry("dk", "Danmark"),
                Map.entry("ea20", "Euroområdet"),
                Map.entry("ee", "Estland"),
                Map.entry("el", "Grekland"),
                Map.entry("es", "Spanien"),
                Map.entry("eu27_2020", "Europeiska unionen (EU27)"),
                Map.entry("fi", "Finland"),
                Map.entry("fr", "Frankrike"),
                Map.entry("hr", "Kroatien"),
                Map.entry("hu", "Ungern"),
                Map.entry("ie", "Irland"),
                Map.entry("it", "Italien"),
                Map.entry("lt", "Litauen"),
                Map.entry("lu", "Luxemburg"),
                Map.entry("lv", "Lettland"),
                Map.entry("mt", "Malta"),
                Map.entry("nl", "Nederländerna"),
                Map.entry("pl", "Polen"),
                Map.entry("pt", "Portugal"),
                Map.entry("ro", "Rumänien"),
                Map.entry("se", "Sverige"),
                Map.entry("si", "Slovenien"),
                Map.entry("sk", "Slovakien")
        );

        // when
        final var debt = macroSeriesQueries.getChartSeriesConfigList().stream()
                .filter(c -> c.category().equals("Statsskuld") && c.name().startsWith("Debt"))
                .toList();

        // then
        assertEquals(2 * countryByGeo.size(), debt.size());
        debt.forEach(c -> {
            final var geo = c.scrapeUrl().substring(c.scrapeUrl().indexOf("&geo=") + "&geo=".length());
            assertEquals(countryByGeo.get(geo), c.country(), c.name());
        });
    }

    private static SeriesConfig find(List<SeriesConfig> configs, String name) {
        return configs.stream()
                .filter(c -> c.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

}
