package com.kg.macroanalyzer.adaptors.web;

import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebAdaptorFactory {

    FedAdaptor fedAdaptor;
    RiksbankenAdaptor riksbankenAdaptor;
    EuroStatAdaptor eurostatAdaptor;
    RiksdagenAdaptor riksdagenAdaptor;

    @Autowired
    public WebAdaptorFactory(
            FedAdaptor fedAdaptor,
            RiksbankenAdaptor riksbankenAdaptor,
            EuroStatAdaptor eurostatAdaptor,
            RiksdagenAdaptor riksdagenAdaptor
    ) {
        this.fedAdaptor = fedAdaptor;
        this.riksbankenAdaptor = riksbankenAdaptor;
        this.eurostatAdaptor = eurostatAdaptor;
        this.riksdagenAdaptor = riksdagenAdaptor;
    }

    public WebAdaptor build(SeriesConfig seriesConfig) {
        return switch (seriesConfig.name()) {
            case String name when name.startsWith("FED") -> fedAdaptor;
            case String name when name.startsWith("Debt") -> eurostatAdaptor;
            case String name when name.equals("MembersOfParliament") -> riksdagenAdaptor;
            default -> riksbankenAdaptor;
        };
    }

}
