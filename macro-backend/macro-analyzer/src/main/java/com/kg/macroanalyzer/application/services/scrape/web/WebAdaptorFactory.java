package com.kg.macroanalyzer.application.services.scrape.web;

import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebAdaptorFactory {

    FedAdaptor fedAdaptor;
    RiksbankenAdaptor riksbankenAdaptor;

    @Autowired
    public WebAdaptorFactory(FedAdaptor fedAdaptor, RiksbankenAdaptor riksbankenAdaptor) {
        this.fedAdaptor = fedAdaptor;
        this.riksbankenAdaptor = riksbankenAdaptor;
    }

    public WebAdaptor build(SeriesConfig seriesConfig) {
        if (seriesConfig.name().startsWith("FED")) return fedAdaptor;
        else return riksbankenAdaptor;
    }

}
