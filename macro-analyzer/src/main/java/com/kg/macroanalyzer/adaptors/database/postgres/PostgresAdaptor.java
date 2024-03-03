package com.kg.macroanalyzer.adaptors.database.postgres;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.*;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
public class PostgresAdaptor implements DatabasePort {

    EuroMarketRateRepository euroMarketRateRepo;
    ExchangeRateRepository exchangeRateRepo;
    GovernmentBillRepository governmentBillRepo;
    GovernmentBondsRepository govBondsRepo;
    PolicyRateRepository policyRateRepo;
    ScrapeRepository scrapeRepository;
    SeriesConfigRepository seriesConfigRepository;

    @Autowired
    public PostgresAdaptor(
            EuroMarketRateRepository euroMarketRateRepo,
            ExchangeRateRepository exchangeRateRepo,
            GovernmentBillRepository governmentBillRepo,
            GovernmentBondsRepository govBondsRepo,
            PolicyRateRepository policyRateRepo,
            ScrapeRepository scrapeRepository,
            SeriesConfigRepository seriesConfigRepository
    ) {
        this.euroMarketRateRepo = euroMarketRateRepo;
        this.exchangeRateRepo = exchangeRateRepo;
        this.governmentBillRepo = governmentBillRepo;
        this.govBondsRepo = govBondsRepo;
        this.policyRateRepo = policyRateRepo;
        this.scrapeRepository = scrapeRepository;
        this.seriesConfigRepository = seriesConfigRepository;
    }

    @Override
    public List<MacroSeries> readMacroSeries(List<SeriesConfig> paramList) {
        return paramList.stream()
                .map(this::buildSeries)
                .toList();
    }

    @Override
    public List<SeriesConfig> readSeriesConfigList() {
        return seriesConfigRepository.readSeriesConfigList();
    }

    @Override
    public void persist(ScrapeQueueItem scrapeQueueItem) {
        log.info("Persisting scrapeQueueItem=%s".formatted(scrapeQueueItem));

        scrapeRepository.addScrapeQueueItem(scrapeQueueItem);
    }

    @Override
    public List<ScrapeQueueItem> getScrapeQueue() {
        return scrapeRepository.getCurrentQueue();
    }

    @Override
    public List<ScrapeQueueItem> getItemsToScrape(LocalDateTime timeStamp) {
        return scrapeRepository.getItemsToScrape(timeStamp);
    }

    private MacroSeries buildSeries(SeriesConfig params) {
        final var name = params.name();
        final var macroPoints = macroPointSupplier(params).get();

        return MacroSeries.builder()
                .name(name)
                .macroPoints(macroPoints)
                .build();
    }

    private Supplier<List<MacroPoint>> macroPointSupplier(SeriesConfig params) {
        final var name = params.name();
        final var errorMsg = String.format("Found unexpected MacroSeries name=%s".formatted(name));

        return switch (params.name()) {
            case "PolicyRateSweden" -> policyRateRepo.policyRateSwedenReader();
            case "UsdSekExchangeRate" -> exchangeRateRepo.getExchangeRateUsdSek();
            case "GovernmentBondSweden2Year" -> govBondsRepo.swedishGovBond2YearReader();
            case "GovernmentBondSweden5Year" -> govBondsRepo.swedishGovBond5YearReader();
            case "GovernmentBondSweden7Year" -> govBondsRepo.swedishGovBond7YearReader();
            case "GovernmentBondSweden10Year" -> govBondsRepo.swedishGovBond10YearReader();
            case "GovernmentBillSweden1Month" -> governmentBillRepo.swedishGovBills1MonthReader();
            case "GovernmentBillSweden3Month" -> governmentBillRepo.swedishGovBills3MonthReader();
            case "GovernmentBillSweden6Month" -> governmentBillRepo.swedishGovBills6MonthReader();
            case "GovernmentBillSweden12Month" -> governmentBillRepo.swedishGovBills12MonthReader();
            case "EuroMarketRateDenmark3Month" -> euroMarketRateRepo.euroMarketRate3MonthDenmark();
            case "EuroMarketRateEur3Month" -> euroMarketRateRepo.euroMarketRate3MonthEur();
            case "EuroMarketRateGB3Month" -> euroMarketRateRepo.euroMarketRate3MonthGB();
            case "EuroMarketRateJapan3Month" -> euroMarketRateRepo.euroMarketRate3MonthJapan();
            case "EuroMarketRateNorway3Month" -> euroMarketRateRepo.euroMarketRate3MonthNorway();
            case "EuroMarketRateUsa3Month" -> euroMarketRateRepo.euroMarketRate3MonthUsa();
            case "EuroMarketRateDenmark6Month" -> euroMarketRateRepo.euroMarketRate6MonthDenmark();
            case "EuroMarketRateEur6Month" -> euroMarketRateRepo.getEuroMarketRate6MonthEur();
            case "EuroMarketRateGB6Month" -> euroMarketRateRepo.getEuroMarketRate6MonthGB();
            case "EuroMarketRateJapan6Month" -> euroMarketRateRepo.getEuroMarketRate6MonthJapan();
            case "EuroMarketRateNorway6Month" -> euroMarketRateRepo.getEuroMarketRate6MonthNorway();
            case "EuroMarketRateUsa6Month" -> euroMarketRateRepo.getEuroMarketRate6MonthUsa();
            case "IntGovBondsEur5Year" -> govBondsRepo.intGovBond5YearReaderEur();
            case "IntGovBondsFrance5Year" -> govBondsRepo.intGovBond5YearReaderFrance();
            case "IntGovBondsGB5Year" -> govBondsRepo.intGovBond5YearReaderGB();
            case "IntGovBondsGermany5Year" -> govBondsRepo.intGovBond5YearReaderGermany();
            case "IntGovBondsJapan5Year" -> govBondsRepo.intGovBond5YearReaderJapan();
            case "IntGovBondsNetherlands5Year" -> govBondsRepo.intGovBond5YearReaderHolland();
            case "IntGovBondsUsa5Year" -> govBondsRepo.intGovBond5YearReaderUSA();
            case "IntGovBondsDenmark10Year" -> govBondsRepo.intGovBond10YearReaderDenmark();
            case "IntGovBondsEur10Year" -> govBondsRepo.intGovBond10YearReaderEur();
            case "IntGovBondsFinland10Year" -> govBondsRepo.intGovBond10YearReaderFinland();
            case "IntGovBondsFrance10Year" -> govBondsRepo.intGovBond10YearReaderFrance();
            case "IntGovBondsGB10Year" -> govBondsRepo.intGovBond10YearReaderGB();
            case "IntGovBondsGermany10Year" -> govBondsRepo.intGovBond10YearReaderGermany();
            case "IntGovBondsJapan10Year" -> govBondsRepo.intGovBond10YearReaderJapan();
            case "IntGovBondsNetherlands10Year" -> govBondsRepo.intGovBond10YearReaderHolland();
            case "IntGovBondsNorway10Year" -> govBondsRepo.intGovBond10YearReaderNorway();
            case "IntGovBondsUsa10Year" -> govBondsRepo.intGovBond10YearReaderUSA();
            default -> throw new IllegalArgumentException(errorMsg);
        };
    }

}
