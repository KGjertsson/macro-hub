package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;

public interface ConnectionBuilder<Config, Connection> {

    Connection buildConnection(Config config) throws ScrapeException;

}
