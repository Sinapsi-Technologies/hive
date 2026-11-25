package io.sinapsi.hive.commons.generators.domain;

import io.sinapsi.hive.commons.generators.domain.DomainGenerator;
import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainAggregateGeneratorTest {

    @Test
    void createsAggregateFromExternalTemplate(@TempDir Path project) throws Exception {

        DomainGenerator generator = new DomainGenerator();

        generator.aggregate(project, HiveConfig.defaults(), null, "Order", "OrderId", List.of("status:String"), false);

        String source = Files.readString(project.resolve("src/main/java/com/example/app/domain/aggregates/Order.java"));

        assertTrue(source.contains("public final class Order"));
        assertTrue(source.contains("private final OrderId id;"));
        assertTrue(source.contains("String status"));
    }

    @Test
    void emitsOptionalLlmInstructionInFactoryMethods(@TempDir Path project) throws Exception {
        new DomainGenerator().aggregate(
                project,
                HiveConfig.defaults(),
                null,
                "Order",
                "OrderId",
                List.of("status:String"),
                false,
                "Keep status DRAFT before confirmation.\nPublish OrderConfirmedEvent on success.");

        String source = Files.readString(project.resolve("src/main/java/com/example/app/domain/aggregates/Order.java"));

        assertTrue(source.contains("// LLM: Keep status DRAFT before confirmation."));
        assertTrue(source.contains("// LLM: Publish OrderConfirmedEvent on success."));
    }

    @Test
    void importsProjectValueObjectsUsedByAggregateFields(@TempDir Path project) throws Exception {
        DomainGenerator generator = new DomainGenerator();
        generator.valueObject(project, HiveConfig.defaults(), null,
                new io.sinapsi.hive.commons.model.ValueObjectSpec(
                        "OrderStatus", "String", false, false, null, null, null, null, null, List.of(), false), false);
        generator.aggregate(project, HiveConfig.defaults(), null, "Order", "OrderId",
                List.of("status:OrderStatus"), false);

        String source = Files.readString(project.resolve("src/main/java/com/example/app/domain/aggregates/Order.java"));

        assertTrue(source.contains("import com.example.app.domain.valueobjects.OrderStatus;"));
    }
}
