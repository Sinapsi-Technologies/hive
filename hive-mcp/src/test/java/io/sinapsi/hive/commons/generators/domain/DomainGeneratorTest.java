package io.sinapsi.hive.commons.generators.domain;

import io.sinapsi.hive.commons.generators.domain.DomainGenerator;
import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainGeneratorTest {

    @Test
    void createsIdentifierFromExternalTemplate(@TempDir Path project) throws Exception {

        DomainGenerator generator = new DomainGenerator();

        generator.identifier(project, HiveConfig.defaults(), null, "UserId", "UUID", false);

        String source = Files.readString(project.resolve("src/main/java/com/example/app/domain/valueobjects/UserId.java"));

        assertTrue(source.contains("public record UserId(UUID value)"));
        assertTrue(source.contains("implements AggregateId<UUID>"));
    }

    @Test
    void importsExistingEnumFromEnumsPackage(@TempDir Path project) throws Exception {
        DomainGenerator generator = new DomainGenerator();

        generator.enumeration(project, HiveConfig.defaults(), null, "OrderStatus", List.of("DRAFT", "PAID"), false);
        generator.event(project, HiveConfig.defaults(), null, "OrderStatusChanged",
                List.of("status:OrderStatus"), false);

        String source = Files.readString(project.resolve(
                "src/main/java/com/example/app/domain/events/OrderStatusChanged.java"));
        assertTrue(source.contains("import com.example.app.domain.enums.OrderStatus;"));
        assertTrue(!source.contains("import com.example.app.domain.valueobjects.OrderStatus;"));
    }
}
