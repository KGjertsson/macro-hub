package com.kg.macroanalyzer.shared.web;

import com.kg.macroanalyzer.shared.web.ScrapeException;

public interface WebPort<Config, Response> {

    Response fetch(Config config) throws ScrapeException;

}
