package com.kg.macroanalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class MacroAnalyzerApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Connected to: " + connection.getMetaData().getURL());
        }
    }

}
