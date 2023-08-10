package com.kg.macroanalyzer.services;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kg.macroanalyzer.models.exchangerate.ExchangeRateUsdSek;
import com.kg.macroanalyzer.models.governmentbills.GovernmentBillItem;
import com.kg.macroanalyzer.models.governmentbonds.GovernmentBondItem;
import com.kg.macroanalyzer.models.policyrate.PolicyRateItem;
import com.kg.macroanalyzer.repositories.ExchangeRateRepository;
import com.kg.macroanalyzer.repositories.GovernmentBillRepository;
import com.kg.macroanalyzer.repositories.GovernmentBondsRepository;
import com.kg.macroanalyzer.repositories.PolicyRateRepository;
import com.kg.macroanalyzer.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ScrapeService {

    private final PolicyRateRepository policyRateRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final GovernmentBillRepository governmentBillRepository;
    private final GovernmentBondsRepository governmentBondsRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ScrapeService(PolicyRateRepository policyRateRepository, ExchangeRateRepository exchangeRateRepository, GovernmentBillRepository governmentBillRepository, GovernmentBondsRepository governmentBondsRepository, ObjectMapper objectMapper) {
        this.policyRateRepository = policyRateRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.governmentBillRepository = governmentBillRepository;
        this.governmentBondsRepository = governmentBondsRepository;
        this.objectMapper = objectMapper;
    }

    public Integer scrapePolicyRateSweden() throws IOException {
        String url = "https://api-test.riksbank.se/swea/v1/Observations/SECBREPOEFF/1994-06-01";
        final var persistedPolicyRateItems = policyRateRepository.getPolicyRateSweden();
        final var novelScrapedItems = scrapeNovelItems(url, persistedPolicyRateItems, PolicyRateItem.class);
        policyRateRepository.insertPolicyRateItemsSweden(novelScrapedItems);
        log.info("Found %s new items from scraping, persisting do db...".formatted(novelScrapedItems.size()));

        return novelScrapedItems.size();
    }

    public Integer scrapeExchangeRateUsdSek() throws IOException {
        final var url = "https://api-test.riksbank.se/swea/v1/Observations/SEKUSDPMI/1993-01-04";
        final var persistedExchangeUsdSek = exchangeRateRepository.getExchangeRateUsdSek();
        final var novelScrapedItems = scrapeNovelItems(url, persistedExchangeUsdSek, ExchangeRateUsdSek.class);
        exchangeRateRepository.insertExchangeRateUsdSek(novelScrapedItems);
        log.info("Found %s new items from scraping, persisting do db...".formatted(novelScrapedItems.size()));

        return novelScrapedItems.size();
    }

    public Integer scrapeGovernmentBillsSweden(String period) throws IOException {
        final var e = ("Expected request parameter period to have one of the values [1, 3, 6, 12] " + "but found %s").formatted(period);

        return switch (period) {
            case "1" -> scrapeSwedishGovernmentBills1Month();
            case "3" -> scrapeSwedishGovernmentBills3Month();
            case "6" -> scrapeSwedishGovernmentBills6Month();
            case "12" -> scrapeSwedishGovernmentBills12Month();
            default -> throw new IllegalArgumentException(e);
        };
    }

    private Integer scrapeSwedishGovernmentBills1Month() throws IOException {
        final var url1Month = "https://api-test.riksbank.se/swea/v1/Observations/SETB1MBENCHC/1983-01-03";
        final var persisted1Month = governmentBillRepository.getSwedishGovernmentBills1Month();
        final var novelScraped1 = scrapeNovelItems(url1Month, persisted1Month, GovernmentBillItem.class);
        log.info("Found %s new 1 month items from scraping, persisting do db...".formatted(novelScraped1.size()));
        governmentBillRepository.insertSwedishGovernmentBills1Month(novelScraped1);

        return novelScraped1.size();
    }

    private Integer scrapeSwedishGovernmentBills3Month() throws IOException {
        final var url3Month = "https://api-test.riksbank.se/swea/v1/Observations/SETB3MBENCH/1983-01-03";
        final var persisted3Month = governmentBillRepository.getSwedishGovernmentBills3Month();
        final var novelScraped3 = scrapeNovelItems(url3Month, persisted3Month, GovernmentBillItem.class);
        log.info("Found %s new 3 month items from scraping, persisting do db...".formatted(novelScraped3.size()));
        governmentBillRepository.insertSwedishGovernmentBills3Month(novelScraped3);

        return novelScraped3.size();
    }

    private Integer scrapeSwedishGovernmentBills6Month() throws IOException {
        final var url6Month = "https://api-test.riksbank.se/swea/v1/Observations/SETB6MBENCH/1984-01-02";
        final var persisted6Month = governmentBillRepository.getSwedishGovernmentBills6Month();
        final var novelScraped6 = scrapeNovelItems(url6Month, persisted6Month, GovernmentBillItem.class);
        log.info("Found %s new 6 month items from scraping, persisting do db...".formatted(novelScraped6.size()));
        governmentBillRepository.insertSwedishGovernmentBills6Month(novelScraped6);

        return novelScraped6.size();
    }

    private Integer scrapeSwedishGovernmentBills12Month() throws IOException {
        final var url12Month = "https://api-test.riksbank.se/swea/v1/Observations/SETB12MBENCH/1983-01-03";
        final var persisted12Month = governmentBillRepository.getSwedishGovernmentBills12Month();
        final var novelScraped12 = scrapeNovelItems(url12Month, persisted12Month, GovernmentBillItem.class);
        log.info("Found %s new 12 month items from scraping, persisting do db...".formatted(novelScraped12.size()));
        governmentBillRepository.insertSwedishGovernmentBills12Month(novelScraped12);

        return novelScraped12.size();
    }

    public Integer scrapeGovernmentBondsSweden(String period) throws IOException {
        final var e = ("Expected request parameter period to have one of the values [2, 5, 7, 10] " + "but found %s").formatted(period);

        return switch (period) {
            case "2" -> scrapeSwedishGovernmentBonds2Month();
            case "5" -> scrapeSwedishGovernmentBonds5Month();
            case "7" -> scrapeSwedishGovernmentBonds7Month();
            case "10" -> scrapeSwedishGovernmentBonds10Month();
            default -> throw new IllegalArgumentException(e);
        };
    }

    private Integer scrapeSwedishGovernmentBonds2Month() throws IOException {
        final var url2Month = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB2YC/1987-01-07";
        final var persisted2Month = governmentBondsRepository.getSwedishGovernmentBonds2Month();
        final var novelScraped2 = scrapeNovelItems(url2Month, persisted2Month, GovernmentBondItem.class);
        log.info("Found %s new 2 month items from scraping, persisting do db...".formatted(novelScraped2.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds2Month(novelScraped2);

        return novelScraped2.size();
    }

    private Integer scrapeSwedishGovernmentBonds5Month() throws IOException {
        final var url5Month = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB5YC/1985-01-02";
        final var persisted5Month = governmentBondsRepository.getSwedishGovernmentBonds5Month();
        final var novelScraped5 = scrapeNovelItems(url5Month, persisted5Month, GovernmentBondItem.class);
        log.info("Found %s new 5 month items from scraping, persisting do db...".formatted(novelScraped5.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds5Month(novelScraped5);

        return novelScraped5.size();
    }

    private Integer scrapeSwedishGovernmentBonds7Month() throws IOException {
        final var url7Month = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB7YC/1987-01-02 ";
        final var persisted7Month = governmentBondsRepository.getSwedishGovernmentBonds7Month();
        final var novelScraped7 = scrapeNovelItems(url7Month, persisted7Month, GovernmentBondItem.class);
        log.info("Found %s new 7 month items from scraping, persisting do db...".formatted(novelScraped7.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds7Month(novelScraped7);

        return novelScraped7.size();
    }

    private Integer scrapeSwedishGovernmentBonds10Month() throws IOException {
        final var url10Month = "https://api-test.riksbank.se/swea/v1/Observations/SEGVB10YC/1987-01-02";
        final var persisted10Month = governmentBondsRepository.getSwedishGovernmentBonds10Month();
        final var novelScraped10 = scrapeNovelItems(url10Month, persisted10Month, GovernmentBondItem.class);
        log.info("Found %s new 10 month items from scraping, persisting do db...".formatted(novelScraped10.size()));
        governmentBondsRepository.insertSwedishGovernmentBonds10Month(novelScraped10);

        return novelScraped10.size();
    }

    private <E> List<E> scrapeNovelItems(String url, List<E> persistedItems, Class<E> itemType) throws IOException {
        final var response = WebUtils.getHTTP(url);
        JavaType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, itemType);
        List<E> resultList = objectMapper.readValue(response, collectionType);

        return resultList.stream().filter(i -> !persistedItems.contains(i)).toList();
    }

}
