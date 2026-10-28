package com.kg.macroanalyzer.application.ports.driving;

import com.kg.macroanalyzer.application.ports.driving.out.OutPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class ChartSeriesConfigIntegrationTest {

    @Autowired
    OutPort outPort;
    @Autowired
    JsonMapper jsonMapper;

    @Test
    void shouldUseTheSwedishCategories_forEveryChartSeries() {
        // when
        final var categories = outPort.getChartSeriesConfigList().stream()
                .map(SeriesConfig::category)
                .collect(Collectors.toSet());

        // then
        assertEquals(Set.of("Räntor", "Statsobligationer", "Statsskuld", "Valuta", "Nyckeltal USA"), categories);
    }

    @Test
    void shouldHaveUnitAndSubcategory_forEveryChartSeries() {
        // when
        final var configs = outPort.getChartSeriesConfigList();

        // then
        assertEquals(110, configs.size());
        configs.forEach(c -> {
            assertNotNull(c.unit(), c.name());
            assertNotNull(c.subcategory(), c.name());
        });
    }

    @Test
    void shouldHaveCountry_forEveryRateAndBond() {
        // when
        final var ratesAndBonds = outPort.getChartSeriesConfigList().stream()
                .filter(c -> Set.of("Räntor", "Statsobligationer").contains(c.category()))
                .toList();

        // then
        assertEquals(39, ratesAndBonds.size());
        ratesAndBonds.forEach(c -> assertNotNull(c.country(), c.name()));
    }

    @Test
    void shouldHaveMaturity_forEveryRateWithATerm() {
        // when
        final var termRates = outPort.getChartSeriesConfigList().stream()
                .filter(c -> Set.of("Euromarknadsränta", "Statsobligation", "Statsskuldväxel").contains(c.subcategory()))
                .toList();

        // then
        assertEquals(37, termRates.size());
        termRates.forEach(c -> assertNotNull(c.maturityMonths(), c.name()));
    }

    @Test
    void shouldSeparateNominalDebtFromDebtToGdp() {
        // when
        final var configs = outPort.getSeriesConfigList();
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
    void shouldQuoteTheExchangeRate_inSekPerUsd() {
        // when
        final var usdSek = find(outPort.getSeriesConfigList(), "UsdSekExchangeRate");

        // then
        assertEquals("Valuta", usdSek.category());
        assertEquals("SEK/USD", usdSek.unit());
    }

    @Test
    void shouldLeaveTheMetadataEmpty_forNonChartConfigs() {
        // when
        final var membersOfParliament = find(outPort.getSeriesConfigList(), "MembersOfParliament");

        // then
        assertNull(membersOfParliament.unit());
        assertNull(membersOfParliament.subcategory());
        assertNull(membersOfParliament.country());
        assertNull(membersOfParliament.maturityMonths());
    }

    @Test
    void shouldSerializeTheMetadata_inCamelCase() {
        // given
        final var bond = find(outPort.getSeriesConfigList(), "GovernmentBondSweden10Year");

        // when
        final var json = jsonMapper.writeValueAsString(bond);

        // then
        assertTrue(json.contains("\"unit\":\"%\""), json);
        assertTrue(json.contains("\"subcategory\":\"Statsobligation\""), json);
        assertTrue(json.contains("\"country\":\"Sverige\""), json);
        assertTrue(json.contains("\"maturityMonths\":120"), json);
    }

    private static SeriesConfig find(List<SeriesConfig> configs, String name) {
        return configs.stream()
                .filter(c -> c.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

}
