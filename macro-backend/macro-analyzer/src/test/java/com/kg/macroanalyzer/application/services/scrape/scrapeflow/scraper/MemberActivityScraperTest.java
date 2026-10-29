package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.application.domain.parliament.MemberActivity;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberActivity;
import com.kg.macroanalyzer.application.ports.driven.MemberActivityRequest;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MemberActivityScraperTest {

    MemberActivityScraper scraper;

    @Mock
    WebPort<MemberActivityRequest, MemberActivity> webPort;

    @BeforeEach
    void setUp() {
        scraper = new MemberActivityScraper(webPort, 0);
    }

    @Test
    void shouldSkipFailedMembers_andKeepTheRest() throws ScrapeException {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var input = new ConfigWithMemberActivity(seriesConfig, List.of("ok", "broken"), List.of());
        final var activity = MemberActivity.builder().intressentId("ok").build();
        final var baseUrl = seriesConfig.scrapeUrl();

        // when
        when(webPort.fetch(new MemberActivityRequest(baseUrl, "ok"))).thenReturn(activity);
        when(webPort.fetch(new MemberActivityRequest(baseUrl, "broken"))).thenThrow(new ScrapeException("reset"));
        final var result = scraper.scrape(input);

        // then
        assertTrue(result.isPresent());
        assertEquals(List.of(activity), result.get().memberActivity());
    }

    @Test
    void shouldFail_whenEveryMemberFails() throws ScrapeException {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var input = new ConfigWithMemberActivity(seriesConfig, List.of("broken"), List.of());

        // when
        when(webPort.fetch(any())).thenThrow(new ScrapeException("reset"));
        final var result = scraper.scrape(input);

        // then
        assertTrue(result.isEmpty());
    }

}
