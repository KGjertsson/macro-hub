package com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeEngine;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScrapeEngineFactory {

    private final PolicyRateRepository policyRateRepository;
    private final GovernmentBillRepository govBillRepository;
    private final GovernmentBondsRepository govBondsRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final EuroMarketRateRepository euroMarketRateRepository;
    private final ScrapeRepository scrapeRepository;

    @Autowired
    public ScrapeEngineFactory(
            PolicyRateRepository policyRateRepository,
            GovernmentBillRepository govBillRepository,
            GovernmentBondsRepository govBondsRepository,
            ExchangeRateRepository exchangeRateRepository,
            EuroMarketRateRepository euroMarketRateRepository,
            ScrapeRepository scrapeRepository
    ) {
        this.policyRateRepository = policyRateRepository;
        this.govBillRepository = govBillRepository;
        this.govBondsRepository = govBondsRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.euroMarketRateRepository = euroMarketRateRepository;
        this.scrapeRepository = scrapeRepository;
    }

    public ScrapeEngine createScrapeEngine(ScrapeQueueItem scrapeQueueItem) {
        final var name = scrapeQueueItem.name().trim();

        return switch (name) {
            case "policy-rate/sweden" -> new ScrapeEnginePolicyRate(
                    scrapeQueueItem,
                    policyRateRepository,
                    scrapeRepository
            );
            case "government-bills/sweden?period=1" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    scrapeRepository,
                    "/SETB1MBENCHC/1983-01-03",
                    govBillRepository.swedishGovBills1MonthReader(),
                    govBillRepository.swedishGovBill1MonthWriter()
            );
            case "government-bills/sweden?period=3" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    scrapeRepository,
                    "/SETB3MBENCH/1983-01-03",
                    govBillRepository.swedishGovBills3MonthReader(),
                    govBillRepository.swedishGovBill3MonthWriter()
            );
            case "government-bills/sweden?period=6" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    scrapeRepository,
                    "/SETB6MBENCH/1984-01-02",
                    govBillRepository.swedishGovBills6MonthReader(),
                    govBillRepository.swedishGovBill6MonthWriter()
            );
            case "government-bills/sweden?period=12" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    scrapeRepository,
                    "/SETB12MBENCH/1983-01-03",
                    govBillRepository.swedishGovBills12MonthReader(),
                    govBillRepository.swedishGovBill12MonthWriter()
            );
            case "government-bonds/sweden?period=2" -> new ScrapeEngineGovBonds(
                    scrapeQueueItem,
                    scrapeRepository,
                    "/SEGVB2YC/1987-01-07",
                    govBondsRepository.swedishGovBond2YearReader(),
                    govBondsRepository.swedishGovBond2YearWriter()

            );
            case "government-bonds/sweden?period=5" -> new ScrapeEngineGovBonds(
                    scrapeQueueItem,
                    scrapeRepository,
                    "/SEGVB5YC/1985-01-02",
                    govBondsRepository.swedishGovBond5YearReader(),
                    govBondsRepository.swedishGovBond5YearWriter()
            );
            case "government-bonds/sweden?period=7" -> new ScrapeEngineGovBonds(
                    scrapeQueueItem,
                    scrapeRepository,
                    "/SEGVB7YC/1987-01-02",
                    govBondsRepository.swedishGovBond7YearReader(),
                    govBondsRepository.swedishGovBond7YearWriter()
            );
            case "government-bonds/sweden?period=10" -> new ScrapeEngineGovBonds(
                    scrapeQueueItem,
                    scrapeRepository,
                    "/SEGVB10YC/1987-01-02",
                    govBondsRepository.swedishGovBond10YearReader(),
                    govBondsRepository.swedishGovBond10YearWriter()
            );
            case "government-bonds/international?period=5year&country=eur" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/EMGVB5Y/1999-01-04",
                            govBondsRepository.intGovBond5YearReaderEur(),
                            govBondsRepository.intGovBond5YearWriterEur()
                    );
            case "government-bonds/international?period=5year&country=gb" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/GBGVB5Y/1987-01-02",
                            govBondsRepository.intGovBond5YearReaderGB(),
                            govBondsRepository.intGovBond5YearWriterGB()
                    );
            case "government-bonds/international?period=5year&country=japan" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/JPGVB5Y/1987-03-23",
                            govBondsRepository.intGovBond5YearReaderJapan(),
                            govBondsRepository.intGovBond5YearWriterJapan()
                    );
            case "government-bonds/international?period=5year&country=france" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/FRGVB5Y/1988-02-08",
                            govBondsRepository.intGovBond5YearReaderFrance(),
                            govBondsRepository.intGovBond5YearWriterFrance()
                    );
            case "government-bonds/international?period=5year&country=germany" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/DEGVB5Y/1987-02-09",
                            govBondsRepository.intGovBond5YearReaderGermany(),
                            govBondsRepository.intGovBond5YearWriterGermany()
                    );
            case "government-bonds/international?period=5year&country=netherlands" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/NLGVB5Y/1987-02-09",
                            govBondsRepository.intGovBond5YearReaderHolland(),
                            govBondsRepository.intGovBond5YearWriterHolland()
                    );
            case "government-bonds/international?period=5year&country=usa" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/USGVB5Y/1987-02-02",
                            govBondsRepository.intGovBond5YearReaderUSA(),
                            govBondsRepository.intGovBond5YearWriterUSA()
                    );
            case "government-bonds/international?period=10year&country=denmark" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/DKGVB10Y/1982-01-04",
                            govBondsRepository.intGovBond10YearReaderDenmark(),
                            govBondsRepository.intGovBond10YearWriterDenmark()
                    );
            case "government-bonds/international?period=10year&country=eur" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/EMGVB10Y/1999-01-04",
                            govBondsRepository.intGovBond10YearReaderEur(),
                            govBondsRepository.intGovBond10YearWriterEur()
                    );
            case "government-bonds/international?period=10year&country=finland" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/FIGVB10Y/1990-04-02",
                            govBondsRepository.intGovBond10YearReaderFinland(),
                            govBondsRepository.intGovBond10YearWriterFinland()
                    );
            case "government-bonds/international?period=10year&country=france" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/FRGVB10Y/1988-02-08",
                            govBondsRepository.intGovBond10YearReaderFrance(),
                            govBondsRepository.intGovBond10YearWriterFrance()
                    );
            case "government-bonds/international?period=10year&country=gb" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/GBGVB10Y/1987-01-02",
                            govBondsRepository.intGovBond10YearReaderGB(),
                            govBondsRepository.intGovBond10YearWriterGB()
                    );
            case "government-bonds/international?period=10year&country=germany" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/DEGVB10Y/1987-02-09",
                            govBondsRepository.intGovBond10YearReaderGermany(),
                            govBondsRepository.intGovBond10YearWriterGermany()
                    );
            case "government-bonds/international?period=10year&country=japan" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/JPGVB10Y/1987-01-05",
                            govBondsRepository.intGovBond10YearReaderJapan(),
                            govBondsRepository.intGovBond10YearWriterJapan()
                    );
            case "government-bonds/international?period=10year&country=netherlands" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/NLGVB10Y/1987-02-09",
                            govBondsRepository.intGovBond10YearReaderHolland(),
                            govBondsRepository.intGovBond10YearWriterHolland()
                    );
            case "government-bonds/international?period=10year&country=norway" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/NOGVB10Y/1990-05-31",
                            govBondsRepository.intGovBond10YearReaderNorway(),
                            govBondsRepository.intGovBond10YearWriterNorway()
                    );
            case "government-bonds/international?period=10year&country=usa" ->
                    new ScrapeEngineGovBonds(
                            scrapeQueueItem,
                            scrapeRepository,
                            "/USGVB10Y/1991-01-02",
                            govBondsRepository.intGovBond10YearReaderUSA(),
                            govBondsRepository.intGovBond10YearWriterUSA()
                    );
            case "exchange-rate/usd-sek" -> new ScrapeEngineExchangeRate(
                    scrapeQueueItem,
                    scrapeRepository,
                    exchangeRateRepository
            );
            case "euro-market-rate?period=3month&country=denmark" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP3MDKK/1981-11-12",
                    euroMarketRateRepository.euroMarketRate3MonthDenmark(),
                    euroMarketRateRepository.insertEuroMarketRate3MonthDenmark()
            );
            case "euro-market-rate?period=3month&country=eur" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP3MEUR/1999-01-04",
                    euroMarketRateRepository.euroMarketRate3MonthEur(),
                    euroMarketRateRepository.insertEuroMarketRate3MonthEur()
            );
            case "euro-market-rate?period=3month&country=gb" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP3MGBP/1979-12-19",
                    euroMarketRateRepository.euroMarketRate3MonthGB(),
                    euroMarketRateRepository.insertEuroMarketRate3MonthGB()
            );
            case "euro-market-rate?period=3month&country=japan" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP3MJPY/1979-11-29",
                    euroMarketRateRepository.euroMarketRate3MonthJapan(),
                    euroMarketRateRepository.insertEuroMarketRate3MonthJapan()
            );
            case "euro-market-rate?period=3month&country=norway" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP3MNOK/1981-11-12",
                    euroMarketRateRepository.euroMarketRate3MonthNorway(),
                    euroMarketRateRepository.insertEuroMarketRate3MonthNorway()
            );
            case "euro-market-rate?period=3month&country=usa" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP3MUSD/1979-11-28",
                    euroMarketRateRepository.euroMarketRate3MonthUsa(),
                    euroMarketRateRepository.insertEuroMarketRate3MonthUsa()
            );
            case "euro-market-rate?period=6month&country=denmark" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP6MDKK/1981-11-12",
                    euroMarketRateRepository.euroMarketRate6MonthDenmark(),
                    euroMarketRateRepository.insertEuroMarketRate6MonthDenmark()
            );
            case "euro-market-rate?period=6month&country=eur" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP6MEUR/1999-01-04",
                    euroMarketRateRepository.getEuroMarketRate6MonthEur(),
                    euroMarketRateRepository.insertEuroMarketRate6MonthEur()
            );
            case "euro-market-rate?period=6month&country=gb" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP6MGBP/1979-11-28",
                    euroMarketRateRepository.getEuroMarketRate6MonthGB(),
                    euroMarketRateRepository.insertEuroMarketRate6MonthGB()
            );
            case "euro-market-rate?period=6month&country=japan" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP3MJPY/1979-11-29",
                    euroMarketRateRepository.getEuroMarketRate6MonthJapan(),
                    euroMarketRateRepository.insertEuroMarketRate6MonthJapan()
            );
            case "euro-market-rate?period=6month&country=norway" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP6MNOK/1981-11-12",
                    euroMarketRateRepository.getEuroMarketRate6MonthNorway(),
                    euroMarketRateRepository.insertEuroMarketRate6MonthNorway()
            );
            case "euro-market-rate?period=6month&country=usa" -> new ScrapeEngineEuroMarketRate(
                    scrapeRepository,
                    scrapeQueueItem,
                    "/EUDP6MUSD/1979-11-28",
                    euroMarketRateRepository.getEuroMarketRate6MonthUsa(),
                    euroMarketRateRepository.insertEuroMarketRate6MonthUsa()
            );
            default -> {
                final var msg = "Found unexpected argument [%s]".formatted(name);

                throw new IllegalArgumentException(msg);
            }
        };
    }

}
