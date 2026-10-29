package com.kg.macroanalyzer.adaptors.database.postgres;

import com.kg.macroanalyzer.adaptors.database.postgres.repositories.*;
import com.kg.macroanalyzer.application.domain.localelection.Governance;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionResult;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
public class PostgresAdaptor implements DatabasePort {

    EuroMarketRateRepository euroMarketRateRepo;
    ExchangeRateRepository exchangeRateRepo;
    GovernmentBillRepository governmentBillRepo;
    GovernmentBondsRepository govBondsRepo;
    PolicyRateRepository policyRateRepo;
    FedRepository fedRepository;
    EurostatRepository eurostatRepository;
    LocalElectionRepository localElectionRepository;

    @Autowired
    public PostgresAdaptor(
            EuroMarketRateRepository euroMarketRateRepo,
            ExchangeRateRepository exchangeRateRepo,
            GovernmentBillRepository governmentBillRepo,
            GovernmentBondsRepository govBondsRepo,
            PolicyRateRepository policyRateRepo,
            FedRepository fedRepository,
            EurostatRepository eurostatRepository,
            LocalElectionRepository localElectionRepository
    ) {
        this.euroMarketRateRepo = euroMarketRateRepo;
        this.exchangeRateRepo = exchangeRateRepo;
        this.governmentBillRepo = governmentBillRepo;
        this.govBondsRepo = govBondsRepo;
        this.policyRateRepo = policyRateRepo;
        this.fedRepository = fedRepository;
        this.eurostatRepository = eurostatRepository;
        this.localElectionRepository = localElectionRepository;
    }

    @Override
    public List<MacroSeries> readMacroSeries(List<SeriesConfig> paramList) {
        return paramList.stream()
                .map(this::readMacroSeries)
                .toList();
    }

    @Override
    public MacroSeries readMacroSeries(SeriesConfig paramList) {
        return buildSeries(paramList);
    }

    @Override
    public Integer writeMacroPoints(ConfigWithMacroPoints configWithMacroPoints) {
        final var seriesConfig = configWithMacroPoints.seriesConfig();
        final var macroPoints = configWithMacroPoints.macroPoints();
        final var writer = macroPointWriter(seriesConfig);
        return writer.apply(macroPoints);
    }

    @Override
    public Set<String> readLocalElectionSourceMd5s(LocalElectionType type) {
        return localElectionRepository.readSourceMd5s(type);
    }

    @Override
    public Integer writeLocalElectionResults(List<LocalElectionResult> results) {
        return localElectionRepository.writeResults(results);
    }

    @Override
    public Optional<Integer> readLatestLocalElectionYear(LocalElectionType type) {
        return localElectionRepository.readLatestYear(type);
    }

    @Override
    public List<LocalElectionResult> readLocalElectionResults(LocalElectionType type, int year) {
        return localElectionRepository.readResults(type, year);
    }

    @Override
    public Optional<LocalElectionResult> readLocalElectionResult(LocalElectionType type, int year, String areaCode) {
        return localElectionRepository.readResult(type, year, areaCode);
    }

    @Override
    public Optional<Governance> readGovernance(LocalElectionType type, int year, String areaCode) {
        return localElectionRepository.readGovernance(type, year, areaCode);
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

        return switch (name) {
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
            case "FEDUnemploymentRate",
                 "FEDGPD",
                 "FEDTotalPublicDebt",
                 "FEDPublicDebtAsGDPPercentage",
                 "FEDSurplusOrDeficit",
                 "FEDEffectiveFederalFundsRate",
                 "FEDSPX500",
                 "FEDDJ",
                 "FEDN100",
                 "FEDVIX",
                 "FEDCPI" -> fedRepository.fedReader(name);
            case String str when str.startsWith("Debt") -> eurostatRepository.eurostatReader(name);
            default -> throw new IllegalArgumentException(errorMsg);
        };
    }

    private Function<List<MacroPoint>, Integer> macroPointWriter(SeriesConfig params) {
        final var name = params.name();
        final var errorMsg = String.format("Found unexpected MacroSeries name=%s".formatted(name));

        return switch (params.name()) {
            case "PolicyRateSweden" -> policyRateRepo.insertPolicyRateItemsSweden();
            case "UsdSekExchangeRate" -> exchangeRateRepo.insertExchangeRateUsdSek();
            case "GovernmentBondSweden2Year" -> govBondsRepo.swedishGovBond2YearWriter();
            case "GovernmentBondSweden5Year" -> govBondsRepo.swedishGovBond5YearWriter();
            case "GovernmentBondSweden7Year" -> govBondsRepo.swedishGovBond7YearWriter();
            case "GovernmentBondSweden10Year" -> govBondsRepo.swedishGovBond10YearWriter();
            case "GovernmentBillSweden1Month" -> governmentBillRepo.swedishGovBill1MonthWriter();
            case "GovernmentBillSweden3Month" -> governmentBillRepo.swedishGovBill3MonthWriter();
            case "GovernmentBillSweden6Month" -> governmentBillRepo.swedishGovBill6MonthWriter();
            case "GovernmentBillSweden12Month" -> governmentBillRepo.swedishGovBill12MonthWriter();
            case "EuroMarketRateDenmark3Month" -> euroMarketRateRepo.insertEuroMarketRate3MonthDenmark();
            case "EuroMarketRateEur3Month" -> euroMarketRateRepo.insertEuroMarketRate3MonthEur();
            case "EuroMarketRateGB3Month" -> euroMarketRateRepo.insertEuroMarketRate3MonthGB();
            case "EuroMarketRateJapan3Month" -> euroMarketRateRepo.insertEuroMarketRate3MonthJapan();
            case "EuroMarketRateNorway3Month" -> euroMarketRateRepo.insertEuroMarketRate3MonthNorway();
            case "EuroMarketRateUsa3Month" -> euroMarketRateRepo.insertEuroMarketRate3MonthUsa();
            case "EuroMarketRateDenmark6Month" -> euroMarketRateRepo.insertEuroMarketRate6MonthDenmark();
            case "EuroMarketRateEur6Month" -> euroMarketRateRepo.insertEuroMarketRate6MonthEur();
            case "EuroMarketRateGB6Month" -> euroMarketRateRepo.insertEuroMarketRate6MonthGB();
            case "EuroMarketRateJapan6Month" -> euroMarketRateRepo.insertEuroMarketRate6MonthJapan();
            case "EuroMarketRateNorway6Month" -> euroMarketRateRepo.insertEuroMarketRate6MonthNorway();
            case "EuroMarketRateUsa6Month" -> euroMarketRateRepo.insertEuroMarketRate6MonthUsa();
            case "IntGovBondsEur5Year" -> govBondsRepo.intGovBond5YearWriterEur();
            case "IntGovBondsFrance5Year" -> govBondsRepo.intGovBond5YearWriterFrance();
            case "IntGovBondsGB5Year" -> govBondsRepo.intGovBond5YearWriterGB();
            case "IntGovBondsGermany5Year" -> govBondsRepo.intGovBond5YearWriterGermany();
            case "IntGovBondsJapan5Year" -> govBondsRepo.intGovBond5YearWriterJapan();
            case "IntGovBondsNetherlands5Year" -> govBondsRepo.intGovBond5YearWriterHolland();
            case "IntGovBondsUsa5Year" -> govBondsRepo.intGovBond5YearWriterUSA();
            case "IntGovBondsDenmark10Year" -> govBondsRepo.intGovBond10YearWriterDenmark();
            case "IntGovBondsEur10Year" -> govBondsRepo.intGovBond10YearWriterEur();
            case "IntGovBondsFinland10Year" -> govBondsRepo.intGovBond10YearWriterFinland();
            case "IntGovBondsFrance10Year" -> govBondsRepo.intGovBond10YearWriterFrance();
            case "IntGovBondsGB10Year" -> govBondsRepo.intGovBond10YearWriterGB();
            case "IntGovBondsGermany10Year" -> govBondsRepo.intGovBond10YearWriterGermany();
            case "IntGovBondsJapan10Year" -> govBondsRepo.intGovBond10YearWriterJapan();
            case "IntGovBondsNetherlands10Year" -> govBondsRepo.intGovBond10YearWriterHolland();
            case "IntGovBondsNorway10Year" -> govBondsRepo.intGovBond10YearWriterNorway();
            case "IntGovBondsUsa10Year" -> govBondsRepo.intGovBond10YearWriterUSA();
            case "FEDUnemploymentRate",
                 "FEDGPD",
                 "FEDTotalPublicDebt",
                 "FEDPublicDebtAsGDPPercentage",
                 "FEDSurplusOrDeficit",
                 "FEDEffectiveFederalFundsRate",
                 "FEDSPX500",
                 "FEDDJ",
                 "FEDN100",
                 "FEDVIX",
                 "FEDCPI" -> fedRepository.fedWriter(name);
            case String str when str.startsWith("Debt") -> eurostatRepository.eurostatWriter(name);
            default -> throw new IllegalArgumentException(errorMsg);
        };
    }

}
