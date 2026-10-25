package com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExistingMemberOfParliamentFinderTest {

    @Test
    void shouldAlwaysContinueFlow_withNoExistingMembers() {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();

        // when
        final var result = new ExistingMemberOfParliamentFinder().findExisting(seriesConfig);

        // then
        assertTrue(result.isPresent());
        assertEquals(seriesConfig, result.get().seriesConfig());
        assertTrue(result.get().membersOfParliament().isEmpty());
    }

}
