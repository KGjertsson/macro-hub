package com.kg.macroanalyzer.configuration;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class JDBCConfiguration {

    @Bean
    public DSLContext dslContext() throws SQLException {
        final var userName = "macrouser";
        final var password = "macropassword";
        final var url = "jdbc:postgresql://localhost:15432/macroanalyzer";
        final var conn = DriverManager.getConnection(url, userName, password);

        return DSL.using(conn, SQLDialect.POSTGRES);
    }

}
