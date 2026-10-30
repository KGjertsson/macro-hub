package com.kg.macroanalyzer;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// Package-by-domain rules. A new module goes into MODULES (and DOMAIN_MODULES if it holds a business domain)
@AnalyzeClasses(packages = ArchitectureTest.ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    static final String ROOT = "com.kg.macroanalyzer";

    // Modules that follow the domain / application / adaptors layout
    private static final List<String> MODULES = List.of("scrape", "macroseries", "regionalstatistics", "parliament", "election");

    // Modules holding one business domain. They build on scrape, shared and the jOOQ model, and are only used from
    // outside where DOMAIN_MODULE_EXTRA_DEPENDENCIES allows it
    private static final List<String> DOMAIN_MODULES = List.of("macroseries", "regionalstatistics", "parliament", "election");

    // What every domain module may use besides itself and classes outside the application
    private static final List<String> DOMAIN_MODULE_DEPENDENCIES = List.of("scrape", "shared", "jooq.generated");

    // What a module may use of other domain modules, on top of DOMAIN_MODULE_DEPENDENCIES. Only in-ports and domain
    // types, never another module's out-ports, services or adaptors
    private static final Map<String, List<String>> DOMAIN_MODULE_EXTRA_DEPENDENCIES = Map.of(
            "election", List.of("parliament.application.port.in", "parliament.domain")
    );

    // Modules every other module may use, and which use none of them
    private static final List<String> SHARED_MODULES = List.of("shared");

    // Generated code that belongs to no module
    private static final List<String> GENERATED_PACKAGES = List.of("jooq.generated");

    // Only the application class sits in the root package; everything else belongs to a module, so no layer-first
    // package can reappear next to them
    @ArchTest
    static final ArchRule everyClassResidesInAModule = classes().that().resideInAPackage(ROOT + "..")
            .and().doNotHaveFullyQualifiedName(MacroAnalyzerApplication.class.getName())
            .should().resideInAnyPackage(concat(concat(MODULES, SHARED_MODULES), GENERATED_PACKAGES).stream()
                    .map(ArchitectureTest::pkg)
                    .toArray(String[]::new))
            .as("every class except the application class resides in a module or in generated code");

    @ArchTest
    static final ArchRule sharedDoesNotDependOnModules = CompositeArchRule.of(
            SHARED_MODULES.stream()
                    .map(shared -> noClasses().that().resideInAPackage(pkg(shared))
                            .should().dependOnClassesThat().resideInAnyPackage(MODULES.stream().map(ArchitectureTest::pkg).toArray(String[]::new))
                            .as("%s does not depend on any domain module".formatted(shared)))
                    .toList()
    );

    @ArchTest
    static final ArchRule domainAndApplicationDoNotDependOnAdaptors = CompositeArchRule.of(
            MODULES.stream()
                    .map(module -> noClasses().that().resideInAnyPackage(pkg(module + ".domain"), pkg(module + ".application"))
                            .should().dependOnClassesThat().resideInAPackage(pkg(module + ".adaptors"))
                            .as("%s domain and application do not depend on %s adaptors".formatted(module, module)))
                    .toList()
    );

    // Ports are the contract the services implement and use, so they only refer to domain and port types
    @ArchTest
    static final ArchRule portsDoNotDependOnServices = CompositeArchRule.of(
            MODULES.stream()
                    .map(module -> noClasses().that().resideInAPackage(pkg(module + ".application.port"))
                            .should().dependOnClassesThat().resideInAPackage(pkg(module + ".application.service"))
                            .as("%s ports do not depend on %s services".formatted(module, module)))
                    .toList()
    );

    @ArchTest
    static final ArchRule domainModulesOnlyDependOnAllowedModules = CompositeArchRule.of(
            DOMAIN_MODULES.stream()
                    .map(module -> {
                        final var allowed = concat(concat(List.of(module), DOMAIN_MODULE_DEPENDENCIES), extraDependencies(module));
                        return classes().that().resideInAPackage(pkg(module))
                                .should().onlyDependOnClassesThat(resideOutsideOfPackage(ROOT + "..")
                                        .or(resideInAnyPackage(allowed.stream().map(ArchitectureTest::pkg).toArray(String[]::new))))
                                .as("%s only depends on %s".formatted(module, String.join(", ", allowed)));
                    })
                    .toList()
    );

    @ArchTest
    static final ArchRule domainModulesAreOnlyUsedWhereAllowed = CompositeArchRule.of(
            DOMAIN_MODULES.stream()
                    .map(module -> classes().that().resideOutsideOfPackage(pkg(module))
                            .should(onlyUseAllowedPartsOf(module))
                            .as("nothing outside %s depends on %s, except the extra dependencies allowed above".formatted(module, module)))
                    .toList()
    );

    // Fails every dependency on the module that the depending class's module does not allow
    private static ArchCondition<JavaClass> onlyUseAllowedPartsOf(String module) {
        return new ArchCondition<>("only use the allowed parts of %s".formatted(module)) {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                final var allowed = allowedExtraDependencies(origin).stream()
                        .map(ArchitectureTest::pkg)
                        .toArray(String[]::new);
                origin.getDirectDependenciesFromSelf().stream()
                        .filter(d -> resideInAPackage(pkg(module)).test(d.getTargetClass()))
                        .filter(d -> !resideInAnyPackage(allowed).test(d.getTargetClass()))
                        .forEach(d -> events.add(SimpleConditionEvent.violated(d, d.getDescription())));
            }
        };
    }

    private static List<String> allowedExtraDependencies(JavaClass origin) {
        return DOMAIN_MODULE_EXTRA_DEPENDENCIES.keySet().stream()
                .filter(module -> origin.getPackageName().equals(ROOT + "." + module)
                        || origin.getPackageName().startsWith(ROOT + "." + module + "."))
                .flatMap(module -> DOMAIN_MODULE_EXTRA_DEPENDENCIES.get(module).stream())
                .toList();
    }

    private static List<String> extraDependencies(String module) {
        return DOMAIN_MODULE_EXTRA_DEPENDENCIES.getOrDefault(module, List.of());
    }

    private static String pkg(String module) {
        return ROOT + "." + module + "..";
    }

    private static List<String> concat(List<String> first, List<String> second) {
        return Stream.concat(first.stream(), second.stream()).toList();
    }

}
