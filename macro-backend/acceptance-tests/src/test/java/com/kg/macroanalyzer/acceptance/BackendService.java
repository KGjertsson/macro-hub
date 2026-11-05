package com.kg.macroanalyzer.acceptance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BackendService(
        String networkAlias,
        String imageProperty,
        String defaultImage,
        int port,
        String database,
        String databasePassword,
        String gatewayUrlVariable,
        Map<String, String> environment
) {

    public static final String FED_SUBSCRIPTION_KEY = "acceptance-fed-key";

    public static final BackendService REGIONAL_STATISTICS = new BackendService(
            "regional-statistics",
            "acceptance.image.regional-statistics",
            "regional-statistics-service:acceptance",
            8082,
            "regional_statistics",
            "regionalstatisticspassword",
            "REGIONAL_STATISTICS_URL",
            Map.of()
    );

    public static final BackendService MACRO_SERIES = new BackendService(
            "macro-series",
            "acceptance.image.macro-series",
            "macro-series-service:acceptance",
            8083,
            "macro_series",
            "macroseriespassword",
            "MACRO_SERIES_URL",
            Map.of("FED_PROD_SUBSCRIPTION_KEY", FED_SUBSCRIPTION_KEY)
    );

    public static final BackendService PARLIAMENT = new BackendService(
            "parliament",
            "acceptance.image.parliament",
            "parliament-service:acceptance",
            8084,
            "parliament",
            "parliamentpassword",
            "PARLIAMENT_URL",
            Map.of()
    );

    public static final BackendService ELECTION = new BackendService(
            "election",
            "acceptance.image.election",
            "election-service:acceptance",
            8085,
            "election",
            "electionpassword",
            "ELECTION_URL",
            Map.of("ELECTION_PARLIAMENT_URL", PARLIAMENT.internalUrl())
    );

    public static final List<BackendService> ALL = List.of(REGIONAL_STATISTICS, MACRO_SERIES, PARLIAMENT, ELECTION);

    public BackendService withEnvironment(Map<String, String> added) {
        final var merged = new HashMap<>(environment);
        merged.putAll(added);

        return new BackendService(
                networkAlias, imageProperty, defaultImage, port, database, databasePassword, gatewayUrlVariable, Map.copyOf(merged));
    }

    public String image() {
        return System.getProperty(imageProperty, defaultImage);
    }

    public String internalUrl() {
        return "http://%s:%d".formatted(networkAlias, port);
    }

    public String jdbcUrl(String postgresHost) {
        return "jdbc:postgresql://%s:5432/%s".formatted(postgresHost, database);
    }

}
