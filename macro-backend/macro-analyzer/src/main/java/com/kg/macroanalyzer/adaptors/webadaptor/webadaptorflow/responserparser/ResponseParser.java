package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;

public interface ResponseParser<ExternalResponse, InternalResponse> {

    InternalResponse parse(ExternalResponse response) throws ScrapeException;

}
