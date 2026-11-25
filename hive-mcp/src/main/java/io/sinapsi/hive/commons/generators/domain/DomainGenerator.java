package io.sinapsi.hive.commons.generators.domain;

import io.sinapsi.hive.commons.model.HiveConfig;
import io.sinapsi.hive.commons.model.ValueObjectSpec;
import io.sinapsi.hive.commons.model.FieldSpec;
import io.sinapsi.hive.commons.support.JavaSourceSupport;
import io.sinapsi.hive.commons.project.MavenPomUpdater;
import io.sinapsi.hive.commons.support.NameResolver;
import io.sinapsi.hive.commons.template.TemplateRenderer;

import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** Domain generation use cases exposed through the MCP adapters. */
public final class DomainGenerator {
    private final NameResolver names = new NameResolver();
    private final TemplateRenderer templates = new TemplateRenderer();
    private final MavenPomUpdater pomUpdater = new MavenPomUpdater();
    private final JavaSourceSupport java = new JavaSourceSupport();

    public List<Path> valueObject(Path root, HiveConfig config, String module,
                                  ValueObjectSpec spec, boolean force) throws IOException {
        String typeName = names.requireJavaTypeName(spec.name());
        List<FieldSpec> fields = java.parseFields(spec.fieldValues()).stream().distinct().toList();
        String contextRoot = contextRootPackage(config, module);
        String packageName = contextRoot + ".domain.valueobjects";
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("domain/valueobjects")
                .resolve(typeName + ".java");
        String valueType = requireScalarType(spec.type());
        String source;

        if (fields.isEmpty()) {
            Map<String, Object> model = new HashMap<>();
            model.put("packageName", packageName);
            model.put("importsBlock", importsFor(valueType));
            model.put("className", typeName);
            model.put("valueType", valueType);
            model.put("notNull", spec.notNull());
            model.put("notBlank", spec.notBlank());
            model.put("min", spec.min());
            model.put("max", spec.max());
            model.put("minLength", spec.minLength());
            model.put("maxLength", spec.maxLength());
            model.put("pattern", spec.pattern());
            model.put("factoryMethod", scalarFactoryMethod(typeName, valueType));

            source = templates.render(
                    "domain/value-object.java.ftl",
                    model
            );
        } else {
            source = templates.render(
                    "domain/value-object-fields.java.ftl",
                    Map.of(
                            "packageName", packageName,
                            "importsBlock", "",
                            "className", typeName,
                            "fields", fields,
                            "factoryMethod", factoryMethod(typeName, fields)
                    )
            );
        }

        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    public List<Path> aggregate(Path root, HiveConfig config, String module, String name,
                                String id, List<String> fields, boolean force) throws IOException {
        return aggregate(root, config, module, name, id, fields, force, null);
    }

    public List<Path> aggregate(Path root, HiveConfig config, String module, String name,
                                String id, List<String> fields, boolean force,
                                String llmInstruction) throws IOException {
        String typeName = names.requireJavaTypeName(name);
        String idType = names.requireJavaTypeName(id);
        List<FieldSpec> fieldSpecs = java.parseFields(fields).stream().distinct().toList();
        String contextRoot = contextRootPackage(config, module);
        String packageName = contextRoot + ".domain.aggregates";
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("domain/aggregates")
                .resolve(typeName + ".java");
        String constructorParameters = idType + " id"
                + fieldSpecs.stream()
                .map(field -> ", " + field.type() + " " + field.name())
                .reduce("", String::concat);
        String source = templates.render(
                "domain/aggregate.java.ftl",
                Map.of(
                        "packageName", packageName,
                        "importsBlock", domainImports(root, contextRoot, fieldSpecs, idType),
                        "className", typeName,
                        "idType", idType,
                        "fields", fieldSpecs,
                        "constructorParameters", constructorParameters,
                        "createMethod", aggregateFactoryMethod(typeName, constructorParameters, fieldSpecs, "create", llmInstruction),
                        "rehydrateMethod", aggregateFactoryMethod(typeName, constructorParameters, fieldSpecs, "rehydrate", llmInstruction)
                )
        );

        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    public List<Path> identifier(Path root, HiveConfig config, String module, String name, String type, boolean force) throws IOException {

        String typeName = names.requireJavaTypeName(name);
        String valueType = requireIdentifierType(type);
        String contextRoot = contextRootPackage(config, module);
        String packageName = contextRoot + ".domain.valueobjects";

        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("domain/valueobjects")
                .resolve(typeName + ".java");

        String source = templates.render(
                "domain/identifier.java.ftl",
                Map.of(
                        "packageName", packageName,
                        "importsBlock", "UUID".equals(valueType) ? "import java.util.UUID;\n" : "",
                        "className", typeName,
                        "valueType", valueType
                )
        );

        if (Files.exists(target) && !force) {
            throw new IOException("Refusing to overwrite existing file: " + target);
        }

        Files.createDirectories(target.getParent());
        Files.writeString(target, source);

        pomUpdater.ensureBasePom(root, config);
        
        return List.of(target);
    }

    private String contextRootPackage(HiveConfig config, String module) {

        if (module == null || module.isBlank()) {
            return config.basePackage();
        }

        return config.basePackage() + ".modules." + names.modulePackageName(module);
    }

    private String requireIdentifierType(String type) {

        if (type == null || type.isBlank()) {
            return "UUID";
        }

        return switch (type) {
            case "UUID", "Long", "String" -> type;
            default -> throw new IllegalArgumentException("Unsupported identifier type: " + type);
        };
    }

    private String scalarFactoryMethod(String typeName, String valueType) {
        return "    public static " + typeName + " of(" + valueType + " value) {\n"
                + "        return new " + typeName + "(value);\n"
                + "    }\n";
    }

    private String factoryMethod(String typeName, List<FieldSpec> fields) {
        String parameters = fields.stream()
                .map(field -> field.type() + " " + field.name())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        String arguments = fields.stream()
                .map(FieldSpec::name)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return "    public static " + typeName + " of(" + parameters + ") {\n"
                + "        return new " + typeName + "(" + arguments + ");\n"
                + "    }\n";
    }

    private String factoryMethod(String typeName, List<FieldSpec> fields, String idType) {
        String parameters = idType + " id" + fields.stream()
                .map(field -> ", " + field.type() + " " + field.name())
                .reduce("", String::concat);
        String arguments = "id" + fields.stream()
                .map(field -> ", " + field.name())
                .reduce("", String::concat);
        return "    public static " + typeName + " of(" + parameters + ") {\n"
                + "        return new " + typeName + "(" + arguments + ");\n"
                + "    }\n";
    }

    private String aggregateFactoryMethod(
            String typeName,
            String constructorParameters,
            List<FieldSpec> fields,
            String methodName,
            String llmInstruction
    ) {
        String arguments = "id" + fields.stream()
                .map(field -> ", " + field.name())
                .reduce("", String::concat);
        return "    public static " + typeName + " " + methodName + "(" + constructorParameters + ") {\n"
                + renderLlmInstruction(llmInstruction, "        ")
                + "        return new " + typeName + "(" + arguments + ");\n"
                + "    }\n";
    }

    private String renderLlmInstruction(String instruction, String indentation) {
        if (instruction == null || instruction.isBlank()) {
            return "";
        }
        return Arrays.stream(instruction.strip().split("\\R"))
                .map(line -> indentation + "// LLM: " + line.stripTrailing() + "\n")
                .reduce("", String::concat);
    }

    private String domainImports(Path root, String contextRoot, List<FieldSpec> fields, String idType) {
        Set<String> imports = new LinkedHashSet<>();
        String standard = java.importsFor(fields.stream().map(FieldSpec::type).toList());
        if (!standard.isBlank()) {
            imports.addAll(Arrays.stream(standard.split("\\R"))
                    .filter(line -> !line.isBlank())
                    .toList());
        }
        for (String type : fields.stream().map(FieldSpec::type).toList()) {
            for (String referenced : java.referencedTypes(type)) {
                if (referenced.contains(".") || isJavaType(referenced)) {
                    continue;
                }
                String packageName = domainPackageFor(root, contextRoot, referenced);
                imports.add("import " + contextRoot + ".domain." + packageName + "." + referenced + ";");
            }
        }
        imports.add("import " + contextRoot + ".domain.valueobjects." + idType + ";");
        return String.join("\n", imports) + "\n\n";
    }

    private String domainFieldImports(Path root, String contextRoot, List<FieldSpec> fields) {
        Set<String> imports = new LinkedHashSet<>();
        String standard = java.importsFor(fields.stream().map(FieldSpec::type).toList());
        if (!standard.isBlank()) {
            imports.addAll(Arrays.stream(standard.split("\\R"))
                    .filter(line -> !line.isBlank())
                    .toList());
        }
        for (String type : fields.stream().map(FieldSpec::type).toList()) {
            for (String referenced : java.referencedTypes(type)) {
                if (referenced.contains(".") || isJavaType(referenced)) {
                    continue;
                }
                String packageName = domainPackageFor(root, contextRoot, referenced);
                Path candidate = root.resolve("src/main/java")
                        .resolve(contextRoot.replace('.', '/'))
                        .resolve("domain").resolve(packageName).resolve(referenced + ".java");
                if (Files.exists(candidate)) {
                    imports.add("import " + contextRoot + ".domain." + packageName + "." + referenced + ";");
                }
            }
        }
        return String.join("\n", imports) + (imports.isEmpty() ? "" : "\n\n");
    }

    private String domainPackageFor(Path root, String contextRoot, String type) {
        Path base = root.resolve("src/main/java").resolve(contextRoot.replace('.', '/')).resolve("domain");
        if (Files.exists(base.resolve("entities").resolve(type + ".java"))) {
            return "entities";
        }
        if (Files.exists(base.resolve("aggregates").resolve(type + ".java"))) {
            return "aggregates";
        }
        if (Files.exists(base.resolve("enums").resolve(type + ".java"))) {
            return "enums";
        }
        return "valueobjects";
    }

    private boolean isJavaType(String type) {
        return Set.of("String", "Integer", "Long", "Boolean", "Double", "Float", "Short", "Byte",
                "Character", "Object", "Void", "BigDecimal", "BigInteger", "Instant", "LocalDate",
                "LocalDateTime", "OffsetDateTime", "ZonedDateTime", "Collection", "List", "Map",
                "Optional", "Set", "UUID").contains(type)
                || Set.of("void", "boolean", "byte", "short", "int", "long", "float", "double", "char")
                .contains(type);
    }

    private String requireScalarType(String type) {

        if (type == null || type.isBlank()) {
            return "String";
        }

        return switch (type) {
            case "String", "UUID", "Integer", "Long", "BigDecimal", "Boolean" -> type;
            default -> throw new IllegalArgumentException("Unsupported value object type: " + type);
        };
    }

    private String importsFor(String type) {

        return switch (type) {
            case "UUID" -> "import java.util.UUID;\n";
            case "BigDecimal" -> "import java.math.BigDecimal;\n";
            default -> "";
        };
    }


    public List<Path> entity(Path root, HiveConfig config, String module, String name, String id, List<String> fields, boolean force) throws IOException {

        String typeName = names.requireJavaTypeName(name);
        String idType = names.requireJavaTypeName(id);
        List<FieldSpec> fieldSpecs = java.parseFields(fields);
        String contextRoot = contextRootPackage(config, module);
        String packageName = contextRoot + ".domain.entities";

        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("domain/entities")
                .resolve(typeName + ".java");

        String constructorParameters = idType + " id"
                + fieldSpecs.stream()
                .map(field -> ", " + field.type() + " " + field.name())
                .reduce("", String::concat);
        
        String source = templates.render(
                "domain/entity.java.ftl",
                Map.of(
                        "packageName", packageName,
                        "importsBlock", domainImports(root, contextRoot, fieldSpecs, idType),
                        "className", typeName,
                        "idType", idType,
                        "fields", fieldSpecs,
                        "constructorParameters", constructorParameters,
                        "factoryMethod", factoryMethod(typeName, fieldSpecs, idType)
                )
        );

        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    private void write(Path target, String source, boolean force) throws IOException {

        if (Files.exists(target) && !force) {
            throw new IOException("Refusing to overwrite existing file: " + target);
        }

        Files.createDirectories(target.getParent());
        Files.writeString(target, source);
    }

    public List<Path> enumeration(Path root, HiveConfig config, String module, String name, List<String> values, boolean force) throws IOException {
        String typeName = names.requireJavaTypeName(name);
        String contextRoot = contextRootPackage(config, module);
        String packageName = contextRoot + ".domain.enums";
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("domain/enums")
                .resolve(typeName + ".java");
        String constants = values.stream()
                .map(value -> value.toUpperCase(Locale.ROOT))
                .reduce((left, right) -> left + ",\n    " + right)
                .orElse("");
        String source = templates.render(
                "domain/enum.java.ftl",
                Map.of("packageName", packageName, "className", typeName, "values", constants)
        );

        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    public List<Path> event(Path root, HiveConfig config, String module, String name, List<String> fields, boolean force) throws IOException {
        String typeName = names.requireJavaTypeName(name);
        List<FieldSpec> fieldSpecs = java.parseFields(fields);
        String contextRoot = contextRootPackage(config, module);
        String packageName = contextRoot + ".domain.events";
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("domain/events")
                .resolve(typeName + ".java");
        String source = templates.render(
                "domain/event.java.ftl",
                Map.of(
                        "packageName", packageName,
                        "importsBlock", domainFieldImports(root, contextRoot, fieldSpecs),
                        "className", typeName,
                        "fields", fieldSpecs,
                        "factoryMethod", factoryMethod(typeName, fieldSpecs)
                )
        );

        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    public List<Path> exception(Path root, HiveConfig config, String module, String name, String message, boolean force) throws IOException {
        String typeName = names.requireJavaTypeName(name);
        String contextRoot = contextRootPackage(config, module);
        String packageName = contextRoot + ".domain.exceptions";
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("domain/exceptions")
                .resolve(typeName + ".java");
        String defaultConstructor = message == null || message.isBlank()
                ? ""
                : "    public " + typeName + "() {\n        super(\"" + message.replace("\"", "\\\"") + "\");\n    }\n\n";
        String source = templates.render(
                "domain/exception.java.ftl",
                Map.of("packageName", packageName, "className", typeName, "defaultConstructor", defaultConstructor)
        );

        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    public List<Path> service(Path root, HiveConfig config, String module, String name, boolean force) throws IOException {
        String typeName = names.requireJavaTypeName(name);
        String contextRoot = contextRootPackage(config, module);
        String packageName = contextRoot + ".domain.services";
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("domain/services")
                .resolve(typeName + ".java");
        String source = templates.render(
                "domain/service.java.ftl",
                Map.of("packageName", packageName, "className", typeName)
        );

        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    public List<Path> snapshot(Path root, HiveConfig config, String module, String name, List<String> fields, boolean force) throws IOException {
        String typeName = names.requireJavaTypeName(name);
        List<FieldSpec> fieldSpecs = java.parseFields(fields);
        String contextRoot = contextRootPackage(config, module);
        String packageName = contextRoot + ".domain.snapshots";
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("domain/snapshots")
                .resolve(typeName + ".java");
        String source = templates.render(
                "domain/snapshot.java.ftl",
                Map.of(
                        "packageName", packageName,
                        "importsBlock", domainFieldImports(root, contextRoot, fieldSpecs),
                        "className", typeName,
                        "fields", fieldSpecs,
                        "factoryMethod", factoryMethod(typeName, fieldSpecs)
                )
        );

        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }
}
