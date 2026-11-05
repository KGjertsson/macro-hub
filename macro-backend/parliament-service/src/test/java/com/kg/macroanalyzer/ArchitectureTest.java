package com.kg.macroanalyzer;

import com.kg.macroanalyzer.platform.jooq.generated.DefaultCatalog;
import com.kg.macroanalyzer.scrape.application.service.ScrapeService;
import com.kg.macroanalyzer.shared.web.WebPort;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.Set;
import java.util.stream.Stream;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AnalyzeClasses(packages = ArchitectureTest.ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    static final String ROOT = "com.kg.macroanalyzer";

    private static final String MODULE = ROOT + ".parliament";
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
            .and().doNotHaveFullyQualifiedName(ParliamentApplication.class.getName())
            .should().resideInAnyPackage(MODULE + "..", SCRAPE + "..", SHARED + "..", PLATFORM_JOOQ + "..")
            .as("every class except the application class resides in a module or in the platform's generated code");

    @ArchTest
    static final ArchRule moduleOnlyDependsOnItselfAndThePlatformCore = classes().that().resideInAPackage(MODULE + "..")
            .should().onlyDependOnClassesThat(resideOutsideOfPackage(ROOT + "..")
                    .or(resideInAnyPackage(MODULE + "..", SCRAPE + "..", SHARED + "..")))
            .as("parliament only depends on itself, scrape and shared");

    @ArchTest
    static final ArchRule platformDoesNotDependOnTheModule = noClasses()
            .that().resideInAnyPackage(SCRAPE + "..", SHARED + "..", PLATFORM_JOOQ + "..")
            .should().dependOnClassesThat().resideInAPackage(MODULE + "..")
            .as("macro-platform does not depend on parliament");

    @ArchTest
    static final ArchRule domainAndApplicationDoNotDependOnAdaptors = noClasses()
            .that().resideInAnyPackage(MODULE + ".domain..", MODULE + ".application..")
            .should().dependOnClassesThat().resideInAPackage(MODULE + ".adaptors..")
            .as("parliament domain and application do not depend on its adaptors");

    @ArchTest
    static final ArchRule onlyPostgresAdaptorsUseTheJooqModel = noClasses()
            .that().resideInAPackage(MODULE + "..")
            .and().resideOutsideOfPackages(MODULE + ".adaptors.postgres..", MODULE + ".jooq.generated..")
            .should().dependOnClassesThat().resideInAPackage(MODULE + ".jooq.generated..")
            .as("only parliament's postgres adaptors use its jOOQ model");

    // Ports are the contract the services implement and use, so they only refer to domain and port types
    @ArchTest
    static final ArchRule portsDoNotDependOnServices = noClasses().that().resideInAPackage(ROOT + "..application.port..")
            .should().dependOnClassesThat().resideInAPackage(ROOT + "..application.service..")
            .as("no port depends on a service");

    private static final Set<String> MUTABLE_FIELDS = Set.of();

    // Lombok's builders are the one place where a field is meant to be assigned more than once
    private static final DescribedPredicate<JavaClass> LOMBOK_BUILDERS = DescribedPredicate.describe("Lombok builders",
            javaClass -> javaClass.getEnclosingClass()
                    .map(enclosing -> javaClass.getSimpleName().equals(enclosing.getSimpleName() + "Builder"))
                    .orElse(false));

    @ArchTest
    static final ArchRule fieldsAreFinal = fields().that().areDeclaredInClassesThat().resideInAnyPackage(MODULE + "..")
            .and().areDeclaredInClassesThat().resideOutsideOfPackage(MODULE + ".jooq.generated..")
            .and().areDeclaredInClassesThat(DescribedPredicate.not(LOMBOK_BUILDERS))
            .and(DescribedPredicate.describe("not allowed to change",
                    (JavaField field) -> !MUTABLE_FIELDS.contains(field.getOwner().getName() + "." + field.getName())))
            .should().beFinal()
            .as("fields in parliament are final, except the listed ones");

}
