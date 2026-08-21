package io.sinapsi.hive.cli.service.blueprint;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.service.BlueprintGenerator;
import io.sinapsi.hive.cli.service.FileScaffolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueprintValidatorTest {
    @TempDir
    Path tempDir;

    private final BlueprintValidator validator = new BlueprintValidator();

    @Test
    void validBlueprintPassesAndGenerateAcceptsSameStructure() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        new FileScaffolder().init(tempDir, config, false, false);
        Path blueprint = write("""
                version: 1

                types:
                  - kind: id
                    name: OrderId
                  - kind: aggregate
                    name: Order
                    id: OrderId
                """);

        BlueprintValidationResult result = validator.validate(blueprint);

        assertTrue(result.valid());
        assertEquals(2, new BlueprintGenerator().generateFile(tempDir, config, blueprint, false).size());
    }

    @Test
    void invalidYamlFails() throws Exception {
        Path blueprint = write("""
                version: 1
                types: [
                """);

        BlueprintValidationResult result = validator.validate(blueprint);

        assertFalse(result.valid());
        assertEquals("BLUEPRINT_PARSE_ERROR", result.errors().getFirst().code());
        assertEquals(2, result.errors().getFirst().line());
    }

    @Test
    void unsupportedKindFailsWithMachineReadableDiagnostic() throws Exception {
        Path blueprint = write("""
                version: 1

                types:
                  - kind: repository
                    name: OrderRepository
                """);

        BlueprintValidationResult result = validator.validate(blueprint);

        assertFalse(result.valid());
        assertEquals("BLUEPRINT_UNSUPPORTED_KIND", result.errors().getFirst().code());
        assertEquals("types[0].kind", result.errors().getFirst().path());
        assertEquals("repository", result.errors().getFirst().kind());
    }

    @Test
    void missingRequiredPropertyFails() throws Exception {
        Path blueprint = write("""
                version: 1

                types:
                  - kind: aggregate
                    name: Order
                """);

        BlueprintValidationResult result = validator.validate(blueprint);

        assertFalse(result.valid());
        assertEquals("BLUEPRINT_REQUIRED_FIELD_MISSING", result.errors().getFirst().code());
        assertEquals("types[0].id", result.errors().getFirst().path());
    }

    @Test
    void invalidFieldValueFails() throws Exception {
        Path blueprint = write("""
                version: 1

                types:
                  - kind: vo
                    name: Email
                    type: String
                    minLength: many
                """);

        BlueprintValidationResult result = validator.validate(blueprint);

        assertFalse(result.valid());
        assertEquals("BLUEPRINT_PARSE_ERROR", result.errors().getFirst().code());
        assertTrue(result.errors().getFirst().message().contains("Expected integer value"));
    }

    @Test
    void validationDoesNotGenerateJavaSource() throws Exception {
        Path blueprint = write("""
                version: 1

                types:
                  - kind: id
                    name: OrderId
                """);

        BlueprintValidationResult result = validator.validate(blueprint);

        assertTrue(result.valid());
        assertFalse(Files.exists(tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/OrderId.java")));
    }

    private Path write(String content) throws Exception {
        Path blueprint = tempDir.resolve("order.yml");
        Files.writeString(blueprint, content);
        return blueprint;
    }
}
