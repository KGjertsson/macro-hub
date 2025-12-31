package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;

public interface WebPort<Config, Response> {

    Response fetch(Config config) throws ScrapeException;

}
