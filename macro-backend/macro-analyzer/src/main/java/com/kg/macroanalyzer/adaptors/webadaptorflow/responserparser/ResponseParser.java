package com.kg.macroanalyzer.adaptors.webadaptorflow.responserparser;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;

public interface ResponseParser<ExternalResponse, InternalResponse> {

    InternalResponse parse(ExternalResponse response) throws ScrapeException;

}
