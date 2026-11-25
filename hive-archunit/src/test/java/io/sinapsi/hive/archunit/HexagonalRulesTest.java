package io.sinapsi.hive.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HexagonalRulesTest {

    private static final String FIXTURES = "io.sinapsi.hive.archunit.fixtures";
    private static final String COMPLIANT = FIXTURES + ".compliant";

    @Test
    void compliantContextSatisfiesAllBaseRules() {
        JavaClasses classes = new ClassFileImporter().importPackages(COMPLIANT);

        assertDoesNotThrow(() -> HexagonalRules.allBaseRules(COMPLIANT).check(classes));
    }

    @Test
    void domainDependingOnApplicationIsRejected() {
        String base = FIXTURES + ".violations.domaindep";
        JavaClasses classes = new ClassFileImporter().importPackages(base);
        ArchRule rule = HexagonalRules.domainShouldNotDependOnApplication(base);

        assertThrows(AssertionError.class, () -> rule.check(classes));
    }

    @Test
    void serviceInsideDomainIsRejected() {
        String base = FIXTURES + ".violations.serviceindomain";
        JavaClasses classes = new ClassFileImporter().importPackages(base);
        ArchRule rule = HexagonalRules.servicesShouldNotResideInDomain(base);

        assertThrows(AssertionError.class, () -> rule.check(classes));
    }

    @Test
    void infrastructureAdapterDependingOnDomainIsRejected() {
        String base = FIXTURES + ".violations.infrastructuredep";
        JavaClasses classes = new ClassFileImporter().importPackages(base);
        ArchRule rule = HexagonalRules.inboundAdaptersShouldNotDependOnDomain(base);

        assertThrows(AssertionError.class, () -> rule.check(classes));
    }

    @Test
    void viewInsideServicesIsRejected() {
        String base = FIXTURES + ".violations.viewplacement";
        JavaClasses classes = new ClassFileImporter().importPackages(base);

        assertThrows(AssertionError.class,
                () -> HexagonalRules.viewsShouldResideInsideInputPortViews(base).check(classes));
    }

    @Test
    void inputPortDependingOnServiceOutputIsRejected() {
        String base = FIXTURES + ".violations.portservicedep";
        JavaClasses classes = new ClassFileImporter().importPackages(base);

        assertThrows(AssertionError.class,
                () -> HexagonalRules.inputPortsShouldNotDependOnServices(base).check(classes));
    }

    @Test
    void contextsWithoutViewsAreAllowed() {
        String base = FIXTURES + ".violations.domaindep";
        JavaClasses classes = new ClassFileImporter().importPackages(base);

        assertDoesNotThrow(() -> HexagonalRules.viewsShouldResideInsideInputPortViews(base).check(classes));
    }

    @Test
    void applicationDependingOnSharedHiveInfrastructureIsRejected() {
        String base = FIXTURES + ".violations.sharedinfra";
        JavaClasses classes = new ClassFileImporter().importPackages(base);

        assertThrows(AssertionError.class,
                () -> HexagonalRules.applicationShouldNotDependOnInfrastructure(base).check(classes));
    }

    @Test
    void domainDependingOnSharedHiveInfrastructureIsRejected() {
        String base = FIXTURES + ".violations.sharedinfra";
        JavaClasses classes = new ClassFileImporter().importPackages(base);

        assertThrows(AssertionError.class,
                () -> HexagonalRules.domainShouldNotDependOnInfrastructure(base).check(classes));
    }

    @Test
    void domainWithJakartaValidationAnnotationsIsRejected() {
        String base = FIXTURES + ".violations.domainvalidation";
        JavaClasses classes = new ClassFileImporter().importPackages(base);

        assertThrows(AssertionError.class,
                () -> HexagonalRules.domainShouldNotDependOnFrameworks(base).check(classes));
    }
}
