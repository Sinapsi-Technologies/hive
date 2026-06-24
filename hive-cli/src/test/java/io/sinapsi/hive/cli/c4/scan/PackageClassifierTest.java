package io.sinapsi.hive.cli.c4.scan;

import io.sinapsi.hive.cli.c4.model.ArchitectureElementType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackageClassifierTest {
    private final PackageClassifier classifier = new PackageClassifier();

    @Test
    void classifiesApplicationLayer() {
        assertEquals(ArchitectureElementType.COMMAND,
                classifier.classify("com.example.app.application.ports.in.commands"));
        assertEquals(ArchitectureElementType.INPUT_PORT,
                classifier.classify("com.example.app.application.ports.in"));
        assertEquals(ArchitectureElementType.OUTPUT_PORT,
                classifier.classify("com.example.app.application.ports.out"));
        assertEquals(ArchitectureElementType.APPLICATION_SERVICE,
                classifier.classify("com.example.app.application.services"));
    }

    @Test
    void classifiesDomainLayer() {
        assertEquals(ArchitectureElementType.DOMAIN_AGGREGATE,
                classifier.classify("com.example.app.domain.aggregates"));
        assertEquals(ArchitectureElementType.DOMAIN_ENTITY,
                classifier.classify("com.example.app.domain.entities"));
        assertEquals(ArchitectureElementType.DOMAIN_VALUE_OBJECT,
                classifier.classify("com.example.app.domain.valueobjects"));
        assertEquals(ArchitectureElementType.DOMAIN_EVENT,
                classifier.classify("com.example.app.domain.events"));
        assertEquals(ArchitectureElementType.DOMAIN_SERVICE,
                classifier.classify("com.example.app.domain.services"));
        assertEquals(ArchitectureElementType.DOMAIN_EXCEPTION,
                classifier.classify("com.example.app.domain.exceptions"));
        assertEquals(ArchitectureElementType.DOMAIN_SNAPSHOT,
                classifier.classify("com.example.app.domain.snapshots"));
        assertEquals(ArchitectureElementType.DOMAIN_MODEL,
                classifier.classify("com.example.app.domain.shared"));
    }

    @Test
    void classifiesInfrastructureAndCrossCutting() {
        assertEquals(ArchitectureElementType.INBOUND_ADAPTER,
                classifier.classify("com.example.app.infrastructure.adapters.in"));
        assertEquals(ArchitectureElementType.INBOUND_ADAPTER,
                classifier.classify("com.example.app.infrastructure.adapters.in.web"));
        assertEquals(ArchitectureElementType.OUTBOUND_ADAPTER,
                classifier.classify("com.example.app.infrastructure.adapters.out"));
        assertEquals(ArchitectureElementType.CONFIGURATION,
                classifier.classify("com.example.app.infrastructure.configs"));
        assertEquals(ArchitectureElementType.CONFIGURATION,
                classifier.classify("com.example.app.configurations"));
        assertEquals(ArchitectureElementType.COMMON,
                classifier.classify("com.example.app.commons"));
    }

    @Test
    void worksWithModularLayout() {
        assertEquals(ArchitectureElementType.INPUT_PORT,
                classifier.classify("com.example.app.modules.todo.application.ports.in"));
        assertEquals(ArchitectureElementType.OUTBOUND_ADAPTER,
                classifier.classify("com.example.app.modules.todo.infrastructure.adapters.out"));
    }

    @Test
    void unknownPackageIsUnknown() {
        assertEquals(ArchitectureElementType.UNKNOWN, classifier.classify("com.example.app.random"));
        assertEquals(ArchitectureElementType.UNKNOWN, classifier.classify(""));
    }

    @Test
    void resultRecordsInServicesPackageAreNotApplicationServices() {
        String servicesPackage = "com.example.app.modules.todo.application.services";
        assertEquals(ArchitectureElementType.APPLICATION_SERVICE,
                classifier.classify(servicesPackage, "CreateTodoService"));
        // A *Result record living next to the services is not a component.
        assertEquals(ArchitectureElementType.UNKNOWN,
                classifier.classify(servicesPackage, "CreateTodoResult"));
    }

    @Test
    void domainWordDoesNotFalselyMatchSubstring() {
        // "domainservice" is a single segment, not the ".domain." convention.
        assertEquals(ArchitectureElementType.UNKNOWN, classifier.classify("com.example.app.domainservice"));
    }
}
