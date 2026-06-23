package io.sinapsi.hive.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.CompositeArchRule.of;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public final class HexagonalRules {

    private HexagonalRules() {
    }

    public static ArchRule domainShouldNotDependOnFrameworks(String basePackage) {
        return noClasses()
                .that()
                .resideInAPackage(packagePattern(basePackage, "domain"))
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "javax.persistence.."
                )
                .because("domain code must stay framework-free and must not depend on Spring or persistence APIs");
    }

    public static ArchRule domainShouldNotDependOnApplication(String basePackage) {
        return noClasses()
                .that()
                .resideInAPackage(packagePattern(basePackage, "domain"))
                .should()
                .dependOnClassesThat()
                .resideInAPackage(packagePattern(basePackage, "application"))
                .because("domain code must not depend on application orchestration");
    }

    public static ArchRule domainShouldNotDependOnInfrastructure(String basePackage) {
        return noClasses()
                .that()
                .resideInAPackage(packagePattern(basePackage, "domain"))
                .should()
                .dependOnClassesThat()
                .resideInAPackage(packagePattern(basePackage, "infrastructure"))
                .because("domain code must not depend on infrastructure adapters or configuration");
    }

    public static ArchRule applicationShouldNotDependOnInfrastructure(String basePackage) {
        return noClasses()
                .that()
                .resideInAPackage(packagePattern(basePackage, "application"))
                .should()
                .dependOnClassesThat()
                .resideInAPackage(packagePattern(basePackage, "infrastructure"))
                .because("application code should depend on ports and interfaces, not infrastructure adapters");
    }

    public static ArchRule applicationShouldNotDependOnInboundAdapters(String basePackage) {
        return noClasses()
                .that()
                .resideInAPackage(packagePattern(basePackage, "application"))
                .should()
                .dependOnClassesThat()
                .resideInAPackage(packagePattern(basePackage, "infrastructure.adapters.in"))
                .because("application code must not depend on inbound adapters");
    }

    public static ArchRule applicationShouldNotDependOnOutboundAdapters(String basePackage) {
        return noClasses()
                .that()
                .resideInAPackage(packagePattern(basePackage, "application"))
                .should()
                .dependOnClassesThat()
                .resideInAPackage(packagePattern(basePackage, "infrastructure.adapters.out"))
                .because("application code must depend on output ports, not outbound adapters");
    }

    public static ArchRule inboundAdaptersShouldNotDependOnOutboundAdapters(String basePackage) {
        return noClasses()
                .that()
                .resideInAPackage(packagePattern(basePackage, "infrastructure.adapters.in"))
                .should()
                .dependOnClassesThat()
                .resideInAPackage(packagePattern(basePackage, "infrastructure.adapters.out"))
                .because("inbound adapters should call input ports or use cases, not outbound adapters directly")
                .allowEmptyShould(true);
    }

    public static ArchRule mappersShouldNotBeInDomain(String basePackage) {
        return noClasses()
                .that()
                .resideInAPackage(packagePattern(basePackage, "domain"))
                .should()
                .haveSimpleNameEndingWith("Mapper")
                .because("mapping is an application or adapter concern, not a domain concern");
    }

    public static ArchRule commandsShouldResideInAllowedPlaces(String basePackage) {
        return classes()
                .that()
                .haveSimpleNameEndingWith("Command")
                .should()
                .resideInAnyPackage(
                        packagePattern(basePackage, "application.ports.in"),
                        packagePattern(basePackage, "application.ports.in.commands")
                )
                .because(
                        "small commands may be nested inside input port or use case contracts, " +
                        "while large commands may be extracted under application.ports.in.commands"
                );
    }

    public static ArchRule noStandaloneApplicationCommandPackageShouldBeUsed(String basePackage) {
        return noClasses()
                .that()
                .resideInAPackage(packagePattern(basePackage, "application.command"))
                .should()
                .resideInAPackage(packagePattern(basePackage, "application.command"))
                .because("commands should live under application.ports.in or application.ports.in.commands")
                .allowEmptyShould(true);
    }

    public static ArchRule useCasesShouldResideInsideInputPorts(String basePackage) {
        return classes()
                .that()
                .haveSimpleNameEndingWith("UseCase")
                .or()
                .haveSimpleNameEndingWith("InputPort")
                .should()
                .resideInAPackage(packagePattern(basePackage, "application.ports.in"))
                .because("input ports and use case contracts should live under application.ports.in");
    }

    public static ArchRule outputPortsShouldResideInsideOutputPortsPackage(String basePackage) {
        return classes()
                .that(outputPortNames())
                .should()
                .resideInAPackage(packagePattern(basePackage, "application.ports.out"))
                .because("outbound ports should live under application.ports.out");
    }

    public static ArchRule servicesShouldNotResideInDomain(String basePackage) {
        return noClasses()
                .that()
                .resideInAPackage(packagePattern(basePackage, "domain"))
                .should()
                .haveSimpleNameEndingWith("Service")
                .because("services should live in application.services or infrastructure, not in domain");
    }

    public static ArchRule noCyclesBetweenMainLayers(String basePackage) {
        return layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .withOptionalLayers(true)

                .layer("Domain")
                .definedBy(packagePattern(basePackage, "domain"))

                .layer("Application")
                .definedBy(packagePattern(basePackage, "application"))

                .layer("InboundAdapters")
                .definedBy(packagePattern(basePackage, "infrastructure.adapters.in"))

                .layer("OutboundAdapters")
                .definedBy(packagePattern(basePackage, "infrastructure.adapters.out"))

                .layer("InfrastructureConfig")
                .definedBy(
                        packagePattern(basePackage, "configurations"),
                        packagePattern(basePackage, "infrastructure.configs")
                )

                .whereLayer("Domain")
                .mayOnlyBeAccessedByLayers(
                        "Application",
                        "InboundAdapters",
                        "OutboundAdapters",
                        "InfrastructureConfig"
                )

                .whereLayer("Application")
                .mayOnlyBeAccessedByLayers(
                        "InboundAdapters",
                        "OutboundAdapters",
                        "InfrastructureConfig"
                )

                .whereLayer("InboundAdapters")
                .mayOnlyBeAccessedByLayers("InfrastructureConfig")

                .whereLayer("OutboundAdapters")
                .mayOnlyBeAccessedByLayers("InfrastructureConfig")

                .whereLayer("InfrastructureConfig")
                .mayNotBeAccessedByAnyLayer()

                .because("dependencies should point inward: adapters depend on application and domain, not the opposite");
    }

    public static ArchRule allBaseRules(String basePackage) {
        return of(domainShouldNotDependOnFrameworks(basePackage))
                .and(domainShouldNotDependOnApplication(basePackage))
                .and(domainShouldNotDependOnInfrastructure(basePackage))
                .and(applicationShouldNotDependOnInfrastructure(basePackage))
                .and(applicationShouldNotDependOnInboundAdapters(basePackage))
                .and(applicationShouldNotDependOnOutboundAdapters(basePackage))
                .and(inboundAdaptersShouldNotDependOnOutboundAdapters(basePackage))
                .and(mappersShouldNotBeInDomain(basePackage))
                .and(commandsShouldResideInAllowedPlaces(basePackage))
                .and(noStandaloneApplicationCommandPackageShouldBeUsed(basePackage))
                .and(useCasesShouldResideInsideInputPorts(basePackage))
                .and(outputPortsShouldResideInsideOutputPortsPackage(basePackage))
                .and(servicesShouldNotResideInDomain(basePackage))
                .and(noCyclesBetweenMainLayers(basePackage));
    }

    private static String packagePattern(String basePackage, String layer) {
        String normalized = normalizeBasePackage(basePackage);

        return normalized.isBlank()
                ? ".." + layer + ".."
                : normalized + ".." + layer + "..";
    }

    private static DescribedPredicate<JavaClass> outputPortNames() {
        return new DescribedPredicate<>("have names ending with Port or OutputPort, except InputPort") {
            @Override
            public boolean test(JavaClass input) {
                String simpleName = input.getSimpleName();
                return simpleName.endsWith("OutputPort")
                        || (simpleName.endsWith("Port") && !simpleName.endsWith("InputPort"));
            }
        };
    }

    private static String normalizeBasePackage(String basePackage) {
        if (basePackage == null || basePackage.isBlank()) {
            return "";
        }

        return basePackage.endsWith(".")
                ? basePackage.substring(0, basePackage.length() - 1)
                : basePackage;
    }
}
