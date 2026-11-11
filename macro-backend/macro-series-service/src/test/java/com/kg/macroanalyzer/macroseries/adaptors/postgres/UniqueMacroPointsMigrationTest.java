package com.kg.macroanalyzer.macroseries.adaptors.postgres;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UniqueMacroPointsMigrationTest {

    private static final String UUID = "gen_random_uuid()";

    @Test
    void shouldKeepTheNewestRowPerSeriesAndDate_andAddTheConstraints() {
        try (final var postgres = new PostgreSQLContainer("postgres:16")) {
            postgres.start();
            final var dataSource = new DriverManagerDataSource(
                    postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()
            );
            final var jdbc = new JdbcTemplate(dataSource);
            final var service = Flyway.configure()
                    .dataSource(dataSource)
                    .baselineOnMigrate(true)
                    .baselineVersion("0");
            // given: at V1, as the service's two Flyway runs leave it
            Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/platform")
                    .table("flyway_platform_schema_history")
                    .load()
                    .migrate();
            Flyway.configure().configuration(service).target("1").load().migrate();
            insertFixture(jdbc);

            // when
            final var applied = service.load().migrate();
            final var secondRun = service.load().migrate();

            // then
            assertEquals(1, applied.migrationsExecuted);
            assertEquals(0, secondRun.migrationsExecuted);
            assertEquals(
                    List.of(
                            Map.of("series_name", "A", "date", "2020-01-01", "value", 3.0),
                            Map.of("series_name", "A", "date", "2020-01-02", "value", 4.0),
                            Map.of("series_name", "A", "date", "2020-01-03", "value", 5.0),
                            Map.of("series_name", "A", "date", "2020-01-04", "value", 6.0),
                            Map.of("series_name", "A", "date", "2020-01-05", "value", 7.0),
                            Map.of("series_name", "B", "date", "2020-01-01", "value", 8.0)
                    ),
                    jdbc.queryForList(
                            "SELECT series_name, date::text AS date, value FROM fed_series "
                                    + "ORDER BY series_name, date"
                    )
            );
            assertEquals(
                    List.of(Map.of("series_name", "A", "date", "2020-01-01", "value", 2.0)),
                    jdbc.queryForList(
                            "SELECT series_name, date::text AS date, value "
                                    + "FROM eurostat_national_debt_series"
                    )
            );
            assertEquals(
                    List.of(2.0),
                    jdbc.queryForList("SELECT value FROM int_gov_bond_10_year_usa", Double.class)
            );
            assertEquals(
                    List.of(2.0),
                    jdbc.queryForList("SELECT policy_rate FROM policy_rate_sweden", Double.class)
            );
            assertEquals(
                    List.of(2.0),
                    jdbc.queryForList("SELECT usd_sek FROM exchange_usd_sek", Double.class)
            );
            assertEquals(
                    List.of(Map.of("definition", "UNIQUE (date)", "tables", 37L),
                            Map.of("definition", "UNIQUE (policy_rate_date)", "tables", 1L),
                            Map.of("definition", "UNIQUE (series_name, date)", "tables", 2L),
                            Map.of("definition", "UNIQUE (usd_sek_date)", "tables", 1L)),
                    jdbc.queryForList("""
                            SELECT pg_get_constraintdef(constraint_row.oid) AS definition, \
                            count(*) AS tables
                            FROM pg_constraint constraint_row
                            JOIN information_schema.columns point_column
                              ON point_column.table_schema = 'public'
                             AND point_column.column_name = 'global_id'
                             AND point_column.table_name = constraint_row.conrelid::regclass::text
                            WHERE constraint_row.contype = 'u'
                            GROUP BY 1
                            ORDER BY 1
                            """)
            );
        }
    }

    private void insertFixture(JdbcTemplate jdbc) {
        final var early = "'2026-10-26 10:00:00'";
        final var early2 = "'2026-10-26 10:00:01'";
        final var late = "'2026-10-29 22:00:00'";
        jdbc.execute("""
                INSERT INTO fed_series (id, global_id, series_name, value, date, created) VALUES
                -- Identical copies and a revision: the revision was created last
                (1, %1$s, 'A', 1.0, '2020-01-01', %2$s),
                (2, %1$s, 'A', 1.0, '2020-01-01', %3$s),
                (3, %1$s, 'A', 3.0, '2020-01-01', %4$s),
                -- The later created wins over the higher id
                (10, %1$s, 'A', 4.0, '2020-01-02', %4$s),
                (11, %1$s, 'A', 99.0, '2020-01-02', %2$s),
                -- The same created: the higher id wins
                (20, %1$s, 'A', 99.0, '2020-01-03', %2$s),
                (21, %1$s, 'A', 5.0, '2020-01-03', %2$s),
                -- No created is older than any created
                (30, %1$s, 'A', 6.0, '2020-01-04', %2$s),
                (31, %1$s, 'A', 99.0, '2020-01-04', NULL),
                -- Identical copies only
                (40, %1$s, 'A', 7.0, '2020-01-05', %2$s),
                (41, %1$s, 'A', 7.0, '2020-01-05', %2$s),
                -- Another series on a date that A has twice is no duplicate
                (50, %1$s, 'B', 8.0, '2020-01-01', %2$s)
                """.formatted(UUID, early, early2, late));
        jdbc.execute("""
                INSERT INTO eurostat_national_debt_series \
                (id, global_id, series_name, value, date, created) VALUES
                (1, %1$s, 'A', 1.0, '2020-01-01', %2$s),
                (2, %1$s, 'A', 2.0, '2020-01-01', %3$s)
                """.formatted(UUID, early, late));
        jdbc.execute("""
                INSERT INTO int_gov_bond_10_year_usa (id, global_id, value, date, created) VALUES
                (1, %1$s, 1.0, '2020-01-01', %2$s),
                (2, %1$s, 2.0, '2020-01-01', %3$s)
                """.formatted(UUID, early, late));
        jdbc.execute("""
                INSERT INTO policy_rate_sweden \
                (id, global_id, policy_rate, policy_rate_date, created) VALUES
                (1, %1$s, 1.0, '2020-01-01', %2$s),
                (2, %1$s, 2.0, '2020-01-01', %3$s)
                """.formatted(UUID, early, late));
        jdbc.execute("""
                INSERT INTO exchange_usd_sek (id, global_id, usd_sek, usd_sek_date, created) VALUES
                (1, %1$s, 1.0, '2020-01-01', %2$s),
                (2, %1$s, 2.0, '2020-01-01', %3$s)
                """.formatted(UUID, early, late));
    }

}
