package com.kg.macroanalyzer;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;

import java.util.List;
import java.util.stream.Stream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// Package-by-domain rules. Add a module to MODULES once its domain slice has moved out of the legacy layer packages
@AnalyzeClasses(packages = ArchitectureTest.ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    static final String ROOT = "com.kg.macroanalyzer";

    // Modules that follow the domain / application / adaptors layout
    private static final List<String> MODULES = List.of("scrape");

    // Modules every other module may use, and which use none of them
    private static final List<String> SHARED_MODULES = List.of("shared");

    // The layer-first packages still being split up by domain
    private static final String[] LEGACY_PACKAGES = {
            ROOT + ".adaptors..",
            ROOT + ".application..",
            ROOT + ".configuration.."
    };

    @ArchTest
    static final ArchRule modulesDoNotDependOnLegacyPackages = CompositeArchRule.of(
            concat(MODULES, SHARED_MODULES).stream()
                    .map(module -> noClasses().that().resideInAPackage(pkg(module))
                            .should().dependOnClassesThat().resideInAnyPackage(LEGACY_PACKAGES)
                            .as("%s does not depend on the legacy layer packages".formatted(module)))
                    .toList()
    );

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

    private static String pkg(String module) {
        return ROOT + "." + module + "..";
    }

    private static List<String> concat(List<String> first, List<String> second) {
        return Stream.concat(first.stream(), second.stream()).toList();
    }

}
