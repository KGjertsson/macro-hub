package com.kg.macroanalyzer.scrape.adaptors.postgres;

import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import org.jooq.DSLContext;
import org.jooq.Record;
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
                .orderBy(SCRAPE_ACTION_QUEUE.ID)
                .fetch()
                .map(this::toScrapeQueueItem);
    }

    public List<ScrapeQueueItem> getCurrentQueue() {
        return dslContext.select()
                .from(SCRAPE_ACTION_QUEUE)
                .where(SCRAPE_ACTION_QUEUE.STATUS.eq(0))
                .fetch()
                .map(this::toScrapeQueueItem);
    }

    public void addScrapeQueueItem(ScrapeQueueItem scrapeQueueItem) {
        final var name = scrapeQueueItem.name();
        final var time = scrapeQueueItem.scrapeDate()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        dslContext.insertInto(
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

    public void markAsDone(String name) {
        dslContext.update(SCRAPE_ACTION_QUEUE)
                .set(SCRAPE_ACTION_QUEUE.STATUS, 1)
                .where(SCRAPE_ACTION_QUEUE.DATASET_NAME.eq(name))
                .execute();
    }

    private ScrapeQueueItem toScrapeQueueItem(Record r) {
        return ScrapeQueueItem.of(
                r.getValue(SCRAPE_ACTION_QUEUE.ID),
                r.getValue(SCRAPE_ACTION_QUEUE.DATASET_NAME),
                r.getValue(SCRAPE_ACTION_QUEUE.SCRAPE_DATE).toInstant(ZoneOffset.UTC)
        );
    }

}
