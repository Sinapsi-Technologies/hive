package io.sinapsi.hive.commons.generators.domain;

import io.sinapsi.hive.commons.generators.domain.DomainGenerator;
import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainRemainingGeneratorsTest {

    @Test
    void createsExceptionFromExternalTemplate(@TempDir Path project) throws Exception {

        DomainGenerator generator = new DomainGenerator();
        generator.exception(project, HiveConfig.defaults(), null, "OrderFailed", "Order failed", false);

        assertTrue(Files.readString(project.resolve("src/main/java/com/example/app/domain/exceptions/OrderFailed.java"))
                .contains("class OrderFailed"));
    }

    @Test
    void createsDomainServiceFromExternalTemplate(@TempDir Path project) throws Exception {

        DomainGenerator generator = new DomainGenerator();
        generator.service(project, HiveConfig.defaults(), null, "Pricing", false);

        assertTrue(Files.readString(project.resolve("src/main/java/com/example/app/domain/services/Pricing.java"))
                .contains("class Pricing"));
    }

    @Test
    void createsSnapshotFromExternalTemplate(@TempDir Path project) throws Exception {

        DomainGenerator generator = new DomainGenerator();
        generator.snapshot(project, HiveConfig.defaults(), null, "OrderSnapshot", List.of("id:UUID"), false);

        assertTrue(Files.readString(project.resolve("src/main/java/com/example/app/domain/snapshots/OrderSnapshot.java"))
                .contains("record OrderSnapshot"));
    }
}
