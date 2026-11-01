package com.kg.macroanalyzer.scrape.adaptors.postgres;

import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** The configs still in the monolith. A service that moves out takes its own with it. */
@SpringBootTest
@ActiveProfiles("test")
class MonolithSeriesConfigIntegrationTest {

    @Autowired
    SeriesConfigPort seriesConfigPort;

    @Test
    void shouldRouteEveryConfig_toItsDomainsScrapeFlow() {
        // when
        final var flowByName = seriesConfigPort.readSeriesConfigList().stream()
                .collect(Collectors.toMap(SeriesConfig::name, SeriesConfig::scrapeFlow));

        // then
        assertEquals(Map.of(
                "MembersOfParliament", "MEMBER_OF_PARLIAMENT",
                "MembersOfParliamentActivity", "MEMBER_ACTIVITY",
                "MunicipalElections", "LOCAL_ELECTION",
                "RegionElections", "LOCAL_ELECTION",
                "ParliamentElections", "LOCAL_ELECTION"
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
        assertNull(membersOfParliament.unit());
        assertNull(membersOfParliament.subcategory());
        assertNull(membersOfParliament.country());
        assertNull(membersOfParliament.maturityMonths());
    }

}
