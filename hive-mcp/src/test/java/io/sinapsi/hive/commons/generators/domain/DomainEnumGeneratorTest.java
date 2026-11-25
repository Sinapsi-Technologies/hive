package io.sinapsi.hive.commons.generators.domain;

import io.sinapsi.hive.commons.generators.domain.DomainGenerator;
import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainEnumGeneratorTest {

    @Test
    void createsEnumFromExternalTemplate(@TempDir Path project) throws Exception {

        DomainGenerator generator = new DomainGenerator();

        generator.enumeration(project, HiveConfig.defaults(), null, "OrderStatus", List.of("draft", "paid"), false);

        String source = Files.readString(project.resolve("src/main/java/com/example/app/domain/enums/OrderStatus.java"));

        assertTrue(source.contains("public enum OrderStatus"));
        assertTrue(source.contains("DRAFT"));
        assertTrue(source.contains("PAID"));
    }
}
