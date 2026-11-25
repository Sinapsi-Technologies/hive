package io.sinapsi.hive.commons.generators.domain;

import io.sinapsi.hive.commons.generators.domain.DomainGenerator;
import io.sinapsi.hive.commons.model.ValueObjectSpec;
import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainValueObjectGeneratorTest {

    @Test
    void createsValueObjectFromExternalTemplate(@TempDir Path project) throws Exception {

        DomainGenerator generator = new DomainGenerator();

        generator.valueObject(
                project,
                HiveConfig.defaults(),
                null,
                new ValueObjectSpec(
                        "Email", "String", false, true, null, null, null, null, null, List.of(), false
                ),
                false
        );

        String source = Files.readString(project.resolve("src/main/java/com/example/app/domain/valueobjects/Email.java"));

        assertTrue(source.contains("public record Email(String value)"));
        assertTrue(source.contains("value.isBlank()"));
    }

    @Test
    void removesDuplicateFieldsWhenRenderingCompositeValueObject(@TempDir Path project) throws Exception {
        new DomainGenerator().valueObject(
                project,
                HiveConfig.defaults(),
                null,
                new ValueObjectSpec(
                        "Address", "String", false, false, null, null, null, null, null,
                        List.of("street:String", "street:String", "city:String"), false
                ),
                false
        );

        String source = Files.readString(project.resolve("src/main/java/com/example/app/domain/valueobjects/Address.java"));
        assertTrue(source.contains("String street,\n    String city"));
        assertTrue(source.contains("String city"));
    }
}
