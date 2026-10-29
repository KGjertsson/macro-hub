package com.kg.macroanalyzer.shared.web;

import com.kg.macroanalyzer.shared.web.ScrapeException;

public interface ResponseParser<ExternalResponse, InternalResponse> {

    InternalResponse parse(ExternalResponse response) throws ScrapeException;

}
