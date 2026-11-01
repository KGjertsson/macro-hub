package com.kg.macroanalyzer.shared.web;

public interface WebPort<Config, Response> {

    Response fetch(Config config) throws ScrapeException;

}
