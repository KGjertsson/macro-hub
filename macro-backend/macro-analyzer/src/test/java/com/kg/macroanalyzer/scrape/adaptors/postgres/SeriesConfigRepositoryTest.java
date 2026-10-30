package com.kg.macroanalyzer.scrape.adaptors.postgres;

import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.kg.macroanalyzer.jooq.generated.Tables.SERIES_CONFIGURATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class SeriesConfigRepositoryTest {

    @Autowired
    SeriesConfigRepository repository;
    @Autowired
    DSLContext dslContext;

    @Test
    void shouldReturnTheConfigs_inIdOrder() {
        // given
        // An update writes a new row version, so the first seeded row is no longer first in physical order
        final var firstId = dslContext.select(SERIES_CONFIGURATION.ID.min()).from(SERIES_CONFIGURATION).fetchOne().value1();
        dslContext.update(SERIES_CONFIGURATION)
                .set(SERIES_CONFIGURATION.DISPLAY_NAME, SERIES_CONFIGURATION.DISPLAY_NAME)
                .where(SERIES_CONFIGURATION.ID.eq(firstId))
                .execute();
        final var namesById = dslContext.select(SERIES_CONFIGURATION.NAME)
                .from(SERIES_CONFIGURATION)
                .orderBy(SERIES_CONFIGURATION.ID)
                .fetch(SERIES_CONFIGURATION.NAME);

        // when
        final var names = repository.readSeriesConfigList().stream()
                .map(SeriesConfig::name)
                .toList();

        // then
        assertEquals(namesById, names);
    }

}
