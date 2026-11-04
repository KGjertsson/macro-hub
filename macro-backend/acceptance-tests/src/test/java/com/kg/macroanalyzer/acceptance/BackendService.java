package com.kg.macroanalyzer.acceptance;

public record BackendService(
        String networkAlias,
        String imageProperty,
        String defaultImage,
        int port,
        String healthPath,
        String database,
        String databasePassword,
        String gatewayUrlVariable
) {

    public static final BackendService MACRO_SERIES = new BackendService(
            "macro-series",
            "acceptance.image.macro-series",
            "macro-series-service:acceptance",
            8083,
            "/macro-analyzer/series-config",
            "macro_series",
            "macroseriespassword",
            "MACRO_SERIES_URL"
    );

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
