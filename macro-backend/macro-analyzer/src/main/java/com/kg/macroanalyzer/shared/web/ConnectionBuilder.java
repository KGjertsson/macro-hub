package com.kg.macroanalyzer.shared.web;

import com.kg.macroanalyzer.shared.web.ScrapeException;

public interface ConnectionBuilder<Config, Connection> {

    Connection buildConnection(Config config) throws ScrapeException;

}
