package io.sinapsi.hive.commons.generators.domain;

import io.sinapsi.hive.commons.generators.domain.DomainGenerator;
import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainEventGeneratorTest {

    @Test
    void createsEventFromExternalTemplate(@TempDir Path project) throws Exception {

        DomainGenerator generator = new DomainGenerator();

        generator.event(project, HiveConfig.defaults(), null, "OrderCreated", List.of("orderId:UUID"), false);

        String source = Files.readString(project.resolve("src/main/java/com/example/app/domain/events/OrderCreated.java"));

        assertTrue(source.contains("public record OrderCreated"));
        assertTrue(source.contains("implements DomainEvent"));
    }
}
