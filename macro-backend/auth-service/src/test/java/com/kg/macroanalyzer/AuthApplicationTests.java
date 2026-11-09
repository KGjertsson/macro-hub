package com.kg.macroanalyzer;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
class AuthApplicationTests {

    @Autowired
    DSLContext dslContext;

    @Test
    void shouldHaveAppliedItsOwnMigrations() {
        // when
        final var versions = dslContext.resultQuery("select version from flyway_schema_history where success order by installed_rank")
                .fetch(0, String.class);

        // then
        assertEquals(List.of("1"), versions);
    }

    @Test
    void shouldHaveNoScrapeTables_becauseItDoesNotScrape() {
        // when
        final var tables = dslContext.resultQuery("select table_name from information_schema.tables where table_schema = 'public'")
                .fetch(0, String.class);

        // then
        assertFalse(tables.contains("scrape_action_queue"), tables::toString);
        assertFalse(tables.contains("flyway_platform_schema_history"), tables::toString);
    }

}
