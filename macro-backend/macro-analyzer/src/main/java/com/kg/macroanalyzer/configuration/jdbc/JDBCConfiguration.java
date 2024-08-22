package com.kg.macroanalyzer.configuration.jdbc;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class JDBCConfiguration {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @ConditionalOnMissingBean(name = "mockedContext")
    public DSLContext dslContext() throws SQLException {
        final var conn = DriverManager.getConnection(url, username, password);

        return DSL.using(conn, SQLDialect.POSTGRES);
    }

    @Bean
    @Profile("test")
    public DSLContext mockedContext() {
        MockDataProvider provider = new MockJDBCConnection();
        MockConnection conn = new MockConnection(provider);

        return DSL.using(conn, SQLDialect.POSTGRES);
    }

}
