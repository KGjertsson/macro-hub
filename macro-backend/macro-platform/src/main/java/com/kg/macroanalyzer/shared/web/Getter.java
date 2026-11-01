package com.kg.macroanalyzer.shared.web;

public interface Getter<Connection, Response> {

    Response get(Connection connection) throws ScrapeException;

}
