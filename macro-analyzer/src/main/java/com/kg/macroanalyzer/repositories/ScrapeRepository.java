package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.ScrapeQueueItem;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;

import static com.kg.macroanalyzer.jooq.generated.Tables.SCRAPE_ACTION_QUEUE;


@Repository
public class ScrapeRepository {

    private final DSLContext dslContext;

    @Autowired
    public ScrapeRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Integer addScrapeQueueItem(ScrapeQueueItem scrapeQueueItem) {
        final var name = scrapeQueueItem.name();
        final var time = scrapeQueueItem.scrapeDate()
                .atZone(ZoneId.of("UTC"))
                .toLocalDateTime();

        return dslContext.insertInto(
                        SCRAPE_ACTION_QUEUE,
                        SCRAPE_ACTION_QUEUE.DATASET_NAME,
                        SCRAPE_ACTION_QUEUE.SCRAPE_DATE)
                .values(name, time)
                .execute();
    }

}
