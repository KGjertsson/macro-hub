package com.kg.macroanalyzer.configuration.jdbc;

import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;

import java.sql.SQLException;

public class MockJDBCConnection implements MockDataProvider {

    @Override
    public MockResult[] execute(MockExecuteContext mockExecuteContext) throws SQLException {
        return new MockResult[0];
    }

}
