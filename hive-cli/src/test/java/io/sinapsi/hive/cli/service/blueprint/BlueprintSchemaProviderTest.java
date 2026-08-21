package io.sinapsi.hive.cli.service.blueprint;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueprintSchemaProviderTest {
    private final BlueprintSchemaProvider provider = new BlueprintSchemaProvider();

    @Test
    void schemaExposesSupportedKindsAndRequiredFields() {
        Map<String, Object> schema = provider.schema(null);
        Map<?, ?> kinds = (Map<?, ?>) schema.get("kinds");
        Map<?, ?> aggregate = (Map<?, ?>) kinds.get("aggregate");
        Map<?, ?> valueObject = (Map<?, ?>) kinds.get("vo");

        assertTrue(kinds.containsKey("id"));
        assertTrue(kinds.containsKey("adapter"));
        assertTrue(kinds.containsKey("outputPort"));
        assertEquals(List.of("kind", "name", "id"), aggregate.get("required"));
        assertTrue(((List<?>) aggregate.get("optional")).contains("fields"));
        assertTrue(((List<?>) valueObject.get("optional")).contains("pattern"));
    }

    @Test
    void schemaExposesConstraintsAndScalarTypes() {
        Map<String, Object> schema = provider.schema(null);
        Map<?, ?> definitions = (Map<?, ?>) schema.get("definitions");

        assertEquals(List.of("String", "UUID", "Integer", "Long", "BigDecimal", "Boolean"),
                definitions.get("scalarValueObjectTypes"));
        assertTrue(((List<?>) definitions.get("valueObjectConstraints")).contains("notBlank"));
    }

    @Test
    void kindFilterReturnsOnlyThatKindAndAcceptsAliases() {
        Map<String, Object> schema = provider.schema("port");
        Map<?, ?> kinds = (Map<?, ?>) schema.get("kinds");
        Map<?, ?> outputPort = (Map<?, ?>) kinds.get("outputPort");

        assertEquals(1, kinds.size());
        assertEquals(List.of("port"), outputPort.get("aliases"));
    }

    @Test
    void everySchemaKindIsResolvableByRegistry() {
        BlueprintKindRegistry registry = new BlueprintKindRegistry();

        for (BlueprintKind kind : provider.kinds()) {
            assertEquals(kind, registry.require(kind.name()));
            for (String alias : kind.aliases()) {
                assertEquals(kind, registry.require(alias));
            }
        }
    }
}
