package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.PolicyRateItem;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static com.kg.macroanalyzer.jooq.generated.Tables.POLICY_RATE_SWEDEN;

@Repository
public class PolicyRateRepository {

    public List<PolicyRateItem> getPolicyRateItems(String country) throws SQLException {
        String userName = "root";
        String password = "";
        String url = "jdbc:mysql://localhost:3306/library";
        Connection conn = DriverManager.getConnection(url, userName, password);
        DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

//        final var foo = create.select()
//                .from(POLICY_RATE_SWEDEN)
//                .fetch()
//                .map(r -> {
//
//                    final var bar = PolicyRateItem.of(r);
//                });

        return null;

    }


}
