package com.kg.macroanalyzer;

// Use the fluent-style API to construct the code generator configuration

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MacroAnalyzerApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MacroAnalyzerApplication.class, args);

        Configuration configuration = new Configuration()
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

    }

}
