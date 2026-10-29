package com.kg.macroanalyzer.shared.web;

import com.kg.macroanalyzer.shared.web.ScrapeException;

public interface Getter<Connection, Response> {

    Response get(Connection connection) throws ScrapeException;

}
