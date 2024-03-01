package com.kg.macroanalyzer.adaptors.database.postgres.repositories;

import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.kg.macroanalyzer.jooq.generated.Tables.SERIES_CONFIGURATION;

@Repository
public class SeriesConfigRepository {

    private final DSLContext dslContext;

    public SeriesConfigRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<SeriesConfig> readSeriesConfigList() {
        return dslContext.select()
                .from(SERIES_CONFIGURATION)
                .fetch()
                .map(this::toSeriesConfig);
    }

    private SeriesConfig toSeriesConfig(Record r) {
        return SeriesConfig.builder()
                .name(r.getValue(SERIES_CONFIGURATION.NAME))
                .displayName(r.getValue(SERIES_CONFIGURATION.DISPLAY_NAME))
                .country(r.getValue(SERIES_CONFIGURATION.COUNTRY))
                .period(r.getValue(SERIES_CONFIGURATION.PERIOD))
                .build();
    }

}
