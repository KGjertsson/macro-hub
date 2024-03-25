package com.kg.macroanalyzer.application.services.scrape;

public enum ScrapeResult {
    SUCCESS,
    FAILED,
    EMPTY;

    public static boolean isSuccess(ScrapeResult result) {
        return result.equals(SUCCESS);
    }

    public static boolean isFailed(ScrapeResult result) {
        return result.equals(FAILED);
    }

    public static boolean isEmpty(ScrapeResult result) {
        return result.equals(EMPTY);
    }

}
