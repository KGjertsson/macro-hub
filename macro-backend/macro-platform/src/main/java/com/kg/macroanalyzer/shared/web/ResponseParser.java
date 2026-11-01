package com.kg.macroanalyzer.shared.web;

public interface ResponseParser<ExternalResponse, InternalResponse> {

    InternalResponse parse(ExternalResponse response) throws ScrapeException;

}
