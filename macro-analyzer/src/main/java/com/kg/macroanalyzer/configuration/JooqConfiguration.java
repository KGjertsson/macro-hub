package com.kg.macroanalyzer.configuration;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.codegen.GenerationTool;
import org.jooq.impl.DSL;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class JooqConfiguration {

    @Bean
    public org.jooq.meta.jaxb.Configuration generateJooqModels() throws Exception {
        org.jooq.meta.jaxb.Configuration configuration = new org.jooq.meta.jaxb.Configuration()
                .withJdbc(new Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl("jdbc:postgresql://localhost:15432/macroanalyzer")
                        .withUser("macrouser")
                        .withPassword("macropassword"))
                .withGenerator(new Generator()
                        .withDatabase(new Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withIncludes(".*")
                                .withExcludes("")
                                .withInputSchema("public")
                                .withCatalogVersionProvider("com.kg.macroanalyzer.configuration.FlywayCatalogVersion")
                                .withSchemaVersionProvider("com.kg.macroanalyzer.configuration.FlywayCatalogVersion")
                        )
                        .withTarget(new Target()
                                .withPackageName("com.kg.macroanalyzer.jooq.generated")
                                .withDirectory("macro-analyzer/src/main/java")));

        GenerationTool.generate(configuration);

        return configuration;
    }

    @Bean
    public DSLContext dslContext() throws SQLException {
        final var userName = "macrouser";
        final var password = "macropassword";
        final var url = "jdbc:postgresql://localhost:15432/macroanalyzer";
        final var conn = DriverManager.getConnection(url, userName, password);

        return DSL.using(conn, SQLDialect.POSTGRES);
    }

}
