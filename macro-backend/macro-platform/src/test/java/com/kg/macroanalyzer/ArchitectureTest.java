package com.kg.macroanalyzer;

import com.kg.macroanalyzer.platform.jooq.generated.DefaultCatalog;
import com.kg.macroanalyzer.scrape.application.service.ScrapeService;
import com.kg.macroanalyzer.shared.web.WebPort;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.stream.Stream;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertTrue;

// The rules of macro-platform itself: scrape, shared and the platform's jOOQ model. Each service's ArchitectureTest adds
// its own domain module on top. Maven already keeps the services' code off this module's classpath, so what these guard
// is the direction between scrape and shared, and the layers of scrape
@AnalyzeClasses(packages = ArchitectureTest.ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    static final String ROOT = "com.kg.macroanalyzer";

    private static final String SCRAPE = ROOT + ".scrape";
    private static final String SHARED = ROOT + ".shared";
    private static final String PLATFORM_JOOQ = ROOT + ".platform.jooq.generated";

    // The rules below hold for classes they see, so an import that finds nothing would pass them silently
    @ArchTest
    static void importIncludesMacroPlatform(JavaClasses classes) {
        Stream.of(ScrapeService.class, WebPort.class, DefaultCatalog.class).forEach(platformClass ->
                assertTrue(classes.contain(platformClass), platformClass.getName() + " is not among the imported classes"));
    }

    @ArchTest
    static final ArchRule everyClassResidesInAModule = classes().that().resideInAPackage(ROOT + "..")
            .should().resideInAnyPackage(SCRAPE + "..", SHARED + "..", PLATFORM_JOOQ + "..")
            .as("every class resides in scrape, shared or the platform's generated code");

    @ArchTest
    static final ArchRule sharedDoesNotDependOnModules = classes().that().resideInAPackage(SHARED + "..")
            .should().onlyDependOnClassesThat(resideOutsideOfPackage(ROOT + "..")
                    .or(resideInAnyPackage(SHARED + "..", PLATFORM_JOOQ + "..")))
            .as("shared does not depend on any module");

    @ArchTest
    static final ArchRule scrapeOnlyDependsOnSharedAndTheModel = classes().that().resideInAPackage(SCRAPE + "..")
            .should().onlyDependOnClassesThat(resideOutsideOfPackage(ROOT + "..")
                    .or(resideInAnyPackage(SCRAPE + "..", SHARED + "..", PLATFORM_JOOQ + "..")))
            .as("scrape only depends on itself, shared and the platform's generated code");

    @ArchTest
    static final ArchRule domainAndApplicationDoNotDependOnAdaptors = noClasses()
            .that().resideInAnyPackage(SCRAPE + ".domain..", SCRAPE + ".application..")
            .should().dependOnClassesThat().resideInAPackage(SCRAPE + ".adaptors..")
            .as("scrape domain and application do not depend on its adaptors");

    // Ports are the contract the services implement and use, so they only refer to domain and port types
    @ArchTest
    static final ArchRule portsDoNotDependOnServices = noClasses().that().resideInAPackage(ROOT + "..application.port..")
            .should().dependOnClassesThat().resideInAPackage(ROOT + "..application.service..")
            .as("no port depends on a service");

}
