package com.kg.macroanalyzer.scrape.adaptors.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates series_configuration and scrape_action_queue in a service's own database: a second Flyway on the same data
 * source, with its own migrations and history table, that runs before the service's own migrations so those can seed
 * the config rows. Every service runs it. Its service run needs spring.flyway.baseline-on-migrate=true and
 * spring.flyway.baseline-version=0, because the schema isn't empty when it starts: without them Flyway stops the start-up
 * ("Found non-empty schema(s) ... but no schema history table").
 */
@Configuration
public class PlatformFlywayConfiguration {

    static final String LOCATION = "classpath:db/platform";
    static final String HISTORY_TABLE = "flyway_platform_schema_history";

    @Bean
    public FlywayMigrationStrategy platformThenServiceMigration() {
        return serviceFlyway -> {
            final var serviceConfiguration = serviceFlyway.getConfiguration();
            Flyway.configure(serviceConfiguration.getClassLoader())
                    .configuration(serviceConfiguration)
                    .locations(LOCATION)
                    .table(HISTORY_TABLE)
                    // The service's baseline settings are for its own run: this one always starts on an empty schema
                    .baselineOnMigrate(false)
                    .load()
                    .migrate();

            serviceFlyway.migrate();
        };
    }

}
