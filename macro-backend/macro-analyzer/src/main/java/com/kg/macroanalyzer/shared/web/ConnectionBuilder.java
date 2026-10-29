package com.kg.macroanalyzer.shared.web;

public interface ConnectionBuilder<Config, Connection> {

    Connection buildConnection(Config config) throws ScrapeException;

}
