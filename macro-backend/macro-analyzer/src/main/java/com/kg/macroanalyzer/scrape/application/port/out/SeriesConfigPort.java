package com.kg.macroanalyzer.scrape.application.port.out;

import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.util.List;

public interface SeriesConfigPort {

    List<SeriesConfig> readSeriesConfigList();

}
