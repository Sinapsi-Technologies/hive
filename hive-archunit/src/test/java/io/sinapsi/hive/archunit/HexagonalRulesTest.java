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
}
