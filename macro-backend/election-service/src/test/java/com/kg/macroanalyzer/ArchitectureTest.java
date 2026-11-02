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

// The layers of this service's one domain module, and its place on top of macro-platform (scrape, shared and the
// platform's jOOQ model), which is imported from the classpath together with this module
@AnalyzeClasses(packages = ArchitectureTest.ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    static final String ROOT = "com.kg.macroanalyzer";

    private static final String MODULE = ROOT + ".election";
    private static final String SCRAPE = ROOT + ".scrape";
    private static final String SHARED = ROOT + ".shared";
    private static final String PLATFORM_JOOQ = ROOT + ".platform.jooq.generated";

    // The rules below hold for classes they see, so a macro-platform missing from the import would pass them silently
    @ArchTest
    static void importIncludesMacroPlatform(JavaClasses classes) {
        Stream.of(ScrapeService.class, WebPort.class, DefaultCatalog.class).forEach(platformClass ->
                assertTrue(classes.contain(platformClass), platformClass.getName() + " is not among the imported classes"));
    }

    @ArchTest
    static final ArchRule everyClassResidesInAModule = classes().that().resideInAPackage(ROOT + "..")
            .and().doNotHaveFullyQualifiedName(ElectionApplication.class.getName())
            .should().resideInAnyPackage(MODULE + "..", SCRAPE + "..", SHARED + "..", PLATFORM_JOOQ + "..")
            .as("every class except the application class resides in a module or in the platform's generated code");

    @ArchTest
    static final ArchRule moduleOnlyDependsOnItselfAndThePlatformCore = classes().that().resideInAPackage(MODULE + "..")
            .should().onlyDependOnClassesThat(resideOutsideOfPackage(ROOT + "..")
                    .or(resideInAnyPackage(MODULE + "..", SCRAPE + "..", SHARED + "..")))
            .as("election only depends on itself, scrape and shared: it reads parliament's party list over HTTP");

    @ArchTest
    static final ArchRule platformDoesNotDependOnTheModule = noClasses()
            .that().resideInAnyPackage(SCRAPE + "..", SHARED + "..", PLATFORM_JOOQ + "..")
            .should().dependOnClassesThat().resideInAPackage(MODULE + "..")
            .as("macro-platform does not depend on election");

    @ArchTest
    static final ArchRule domainAndApplicationDoNotDependOnAdaptors = noClasses()
            .that().resideInAnyPackage(MODULE + ".domain..", MODULE + ".application..")
            .should().dependOnClassesThat().resideInAPackage(MODULE + ".adaptors..")
            .as("election domain and application do not depend on its adaptors");

    @ArchTest
    static final ArchRule onlyPostgresAdaptorsUseTheJooqModel = noClasses()
            .that().resideInAPackage(MODULE + "..")
            .and().resideOutsideOfPackages(MODULE + ".adaptors.postgres..", MODULE + ".jooq.generated..")
            .should().dependOnClassesThat().resideInAPackage(MODULE + ".jooq.generated..")
            .as("only election's postgres adaptors use its jOOQ model");

    // Ports are the contract the services implement and use, so they only refer to domain and port types
    @ArchTest
    static final ArchRule portsDoNotDependOnServices = noClasses().that().resideInAPackage(ROOT + "..application.port..")
            .should().dependOnClassesThat().resideInAPackage(ROOT + "..application.service..")
            .as("no port depends on a service");

}
