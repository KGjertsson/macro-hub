package com.kg.macroanalyzer.models.ScrapeEngine;

import com.kg.macroanalyzer.models.ScrapeQueueItem;
import com.kg.macroanalyzer.repositories.GovernmentBillRepository;
import com.kg.macroanalyzer.repositories.GovernmentBondsRepository;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.repositories.ScrapeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScrapeEngineFactory {

    private final PolicyRateRepository policyRateRepository;
    private final GovernmentBillRepository govBillRepository;
    private final GovernmentBondsRepository govBondsRepository;
    private final ScrapeRepository scrapeRepository;

    @Autowired
    public ScrapeEngineFactory(
            PolicyRateRepository policyRateRepository,
            GovernmentBillRepository govBillRepository,
            GovernmentBondsRepository govBondsRepository,
            ScrapeRepository scrapeRepository
    ) {
        this.policyRateRepository = policyRateRepository;
        this.govBillRepository = govBillRepository;
        this.govBondsRepository = govBondsRepository;
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
                    govBillRepository,
                    scrapeRepository,
                    "1"
            );
            case "government-bills/sweden?period=3" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    govBillRepository,
                    scrapeRepository,
                    "3"
            );
            case "government-bills/sweden?period=6" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    govBillRepository,
                    scrapeRepository,
                    "6"
            );
            case "government-bills/sweden?period=12" -> new ScrapeEngineGovBills(
                    scrapeQueueItem,
                    govBillRepository,
                    scrapeRepository,
                    "12"
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
            default -> {
                final var msg = "Found unexpected argument [%s]".formatted(name);

                throw new IllegalArgumentException(msg);
            }
        };
    }

}
