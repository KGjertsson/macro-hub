package com.kg.macroanalyzer.configuration;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqGenerationConfig {

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
                                .withInputSchema("public"))
                        .withTarget(new Target()
                                .withPackageName("com.kg.macroanalyzer.jooq.generated")
                                .withDirectory("macro-analyzer/src/main/java")));

        GenerationTool.generate(configuration);

        return configuration;
    }

}
