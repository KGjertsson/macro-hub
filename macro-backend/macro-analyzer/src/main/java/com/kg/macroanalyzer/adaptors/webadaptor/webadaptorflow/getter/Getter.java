package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.getter;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;

public interface Getter<Connection, InternalResponse> {

    InternalResponse get(Connection connection) throws ScrapeException;

}
