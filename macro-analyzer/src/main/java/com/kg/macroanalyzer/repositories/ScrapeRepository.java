package com.kg.macroanalyzer.repositories;

import com.kg.macroanalyzer.models.ScrapeQueueItem;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.kg.macroanalyzer.jooq.generated.Tables.SCRAPE_ACTION_QUEUE;


@Repository
public class ScrapeRepository {

    private final DSLContext dslContext;

    @Autowired
    public ScrapeRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<ScrapeQueueItem> getItemsToScrape(LocalDateTime now) {
        return dslContext.select()
                .from(SCRAPE_ACTION_QUEUE)
                .where(SCRAPE_ACTION_QUEUE.SCRAPE_DATE.lessThan(now))
                .and(SCRAPE_ACTION_QUEUE.STATUS.eq(0))
                .fetch()
                .map(ScrapeQueueItem::of);
    }

    public Integer addScrapeQueueItem(ScrapeQueueItem scrapeQueueItem) {
        final var name = scrapeQueueItem.name();
        final var time = scrapeQueueItem.scrapeDate()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        return dslContext.insertInto(
                        SCRAPE_ACTION_QUEUE,
                        SCRAPE_ACTION_QUEUE.DATASET_NAME,
                        SCRAPE_ACTION_QUEUE.SCRAPE_DATE)
                .values(name, time)
                .execute();
    }

    public void markAsDone(Integer id) {
        dslContext.update(SCRAPE_ACTION_QUEUE)
                .set(SCRAPE_ACTION_QUEUE.STATUS, 1)
                .where(SCRAPE_ACTION_QUEUE.ID.eq(id))
                .execute();
    }

}
