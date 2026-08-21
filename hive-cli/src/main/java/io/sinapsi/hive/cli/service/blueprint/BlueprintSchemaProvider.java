package io.sinapsi.hive.cli.service.blueprint;

import io.sinapsi.hive.cli.service.HiveVersions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BlueprintSchemaProvider {
    public static final String SCHEMA_VERSION = "1";
    public static final String BLUEPRINT_VERSION = "1";

    private final BlueprintKindRegistry kinds = new BlueprintKindRegistry();

    public Map<String, Object> schema(String kindFilter) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("command", "blueprint schema");
        schema.put("hiveVersion", HiveVersions.hive());
        schema.put("schemaVersion", SCHEMA_VERSION);
        schema.put("blueprintVersion", BLUEPRINT_VERSION);
        schema.put("root", rootSchema());
        schema.put("definitions", definitions());

        Map<String, Object> kindSchemas = new LinkedHashMap<>();
        List<BlueprintKind> selected = kindFilter == null || kindFilter.isBlank()
                ? kinds.kinds()
                : List.of(kinds.require(kindFilter));
        for (BlueprintKind kind : selected) {
            kindSchemas.put(kind.name(), kindSchema(kind));
        }
        schema.put("kinds", kindSchemas);
        return schema;
    }

    public List<BlueprintKind> kinds() {
        return kinds.kinds();
    }

    private Map<String, Object> rootSchema() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("type", "object");
        root.put("required", List.of("version", "types"));
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("version", Map.of("type", "string", "const", BLUEPRINT_VERSION));
        properties.put("types", Map.of("type", "array", "items", "Blueprint artifact object"));
        root.put("properties", properties);
        root.put("additionalProperties", true);
        return root;
    }

    private Map<String, Object> kindSchema(BlueprintKind kind) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("kind", kind.name());
        schema.put("aliases", kind.aliases());
        schema.put("required", kind.required());
        schema.put("optional", kind.optional());
        schema.put("additionalProperties", true);

        Map<String, Object> properties = new LinkedHashMap<>();
        for (String property : java.util.stream.Stream.concat(kind.required().stream(), kind.optional().stream()).toList()) {
            properties.put(property, propertySchema(property, kind));
        }
        schema.put("properties", properties);
        return schema;
    }

    private Map<String, Object> definitions() {
        Map<String, Object> definitions = new LinkedHashMap<>();
        definitions.put("field", Map.of(
                "type", "object",
                "required", List.of("name", "type"),
                "properties", Map.of(
                        "name", Map.of("type", "string"),
                        "type", Map.of("type", "string")
                )
        ));
        definitions.put("method", Map.of(
                "type", "object",
                "required", List.of("returnType", "name"),
                "properties", Map.of(
                        "returnType", Map.of("type", "string"),
                        "name", Map.of("type", "string"),
                        "parameters", Map.of("type", "array", "items", "field")
                )
        ));
        definitions.put("scalarValueObjectTypes", List.of("String", "UUID", "Integer", "Long", "BigDecimal", "Boolean"));
        definitions.put("valueObjectConstraints", List.of(
                "notNull", "notBlank", "min", "max", "minLength", "maxLength", "pattern"
        ));
        definitions.put("classOptions", List.of("getters", "setters", "constructor", "allArgsConstructor"));
        return definitions;
    }

    private Map<String, Object> propertySchema(String property, BlueprintKind kind) {
        return switch (property) {
            case "kind" -> {
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("type", "string");
                value.put("enum", kind.allKindValues());
                yield value;
            }
            case "fields" -> arrayOf("field");
            case "methods" -> arrayOf("method");
            case "ports", "values" -> arrayOf("string");
            case "notNull", "notBlank", "factory", "getters", "setters", "constructor", "allArgsConstructor" ->
                    Map.of("type", "boolean");
            case "minLength", "maxLength" -> Map.of("type", "integer");
            default -> Map.of("type", "string");
        };
    }

    private Map<String, Object> arrayOf(String item) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("type", "array");
        value.put("items", item);
        return value;
    }
}
