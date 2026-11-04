package com.kg.macroanalyzer.shared.config;

import com.kg.macroanalyzer.MacroSeriesApplication;
import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.net.ConnectException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.kg.macroanalyzer.platform.jooq.generated.Tables.SERIES_CONFIGURATION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * On its own container, because dropping every connection on the test profile's shared one would hit the other cached
 * contexts too. Stopped by Testcontainers' Ryuk, when the JVM exits.
 */
@SpringBootTest(properties = "scheduler.enabled=false")
@ActiveProfiles("test")
@DirtiesContext
class ConnectionPoolIntegrationTest {

    private static final String MARKER = "changed in an open transaction";
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    DSLContext dslContext;
    @Autowired
    DataSource dataSource;
    @Autowired
    SeriesConfigPort seriesConfigPort;

    @Test
    void shouldUseThePostgresDialect_andTheConfiguredPool() throws SQLException {
        final var pool = dataSource.unwrap(HikariDataSource.class);

        assertEquals(SQLDialect.POSTGRES, dslContext.dialect());
        assertEquals(8, pool.getMaximumPoolSize());
        assertEquals(2, pool.getMinimumIdle());
    }

    @Test
    void shouldWorkAgain_afterItsConnectionsAreDropped() throws Exception {
        // given
        final var pool = dataSource.unwrap(HikariDataSource.class);
        dslContext.selectOne().fetch();

        // when
        final var dropped = dropTheServicesConnections();
        // A connection used in the last 500 ms is handed out unchecked. After that the pool tests it, and replaces a dead one
        TimeUnit.SECONDS.sleep(1);

        // then: the first calls already work, with new connections
        assertFalse(dropped.isEmpty());
        assertFalse(dropped.contains(dslContext.fetchSingle("select pg_backend_pid()").get(0, Integer.class)));
        assertFalse(seriesConfigPort.readSeriesConfigList().isEmpty());
        assertTrue(dslContext.transactionResult(configuration -> DSL.using(configuration).fetchCount(SERIES_CONFIGURATION)) > 0);
        assertTrue(pool.getHikariPoolMXBean().getTotalConnections() <= 8);
    }

    // With one shared connection, the read would see the uncommitted change and be undone with it
    @Test
    void shouldNeitherBlockNorShowAnOpenTransaction_toAConcurrentRead() {
        // when
        assertThrows(IllegalStateException.class, () -> dslContext.transaction(configuration -> {
            DSL.using(configuration).update(SERIES_CONFIGURATION)
                    .set(SERIES_CONFIGURATION.CATEGORY, MARKER)
                    .execute();
            assertEquals(0, CompletableFuture.supplyAsync(this::rowsWithTheMarker).get(10, TimeUnit.SECONDS));
            assertTrue(DSL.using(configuration).fetchCount(SERIES_CONFIGURATION, SERIES_CONFIGURATION.CATEGORY.eq(MARKER)) > 0);

            throw new IllegalStateException("roll back");
        }));

        // then
        assertEquals(0, rowsWithTheMarker());
    }

    // Port 1: nothing listens there. The arguments replace the test profile's and application.properties' database
    @Test
    void shouldFailAtStartUp_whenTheDatabaseIsUnreachable() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(MacroSeriesApplication.class).run(
                "--spring.datasource.url=jdbc:postgresql://localhost:1/unreachable",
                "--server.port=0",
                "--scheduler.enabled=false"
        )).hasRootCauseInstanceOf(ConnectException.class);
    }

    private int rowsWithTheMarker() {
        return dslContext.fetchCount(SERIES_CONFIGURATION, SERIES_CONFIGURATION.CATEGORY.eq(MARKER));
    }

    // As the container's superuser, on its own connection, so this one survives
    private static List<Integer> dropTheServicesConnections() throws SQLException {
        final var dropped = new ArrayList<Integer>();
        try (final var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             final var statement = connection.createStatement();
             final var result = statement.executeQuery("""
                     select pid, pg_terminate_backend(pid)
                     from pg_stat_activity
                     where datname = current_database() and pid <> pg_backend_pid()
                     """)) {
            while (result.next()) {
                dropped.add(result.getInt(1));
            }
        }

        return dropped;
    }

}
