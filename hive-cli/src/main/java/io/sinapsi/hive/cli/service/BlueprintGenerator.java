package io.sinapsi.hive.cli.service;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.service.FileScaffolder.ClassSpec;
import io.sinapsi.hive.cli.service.FileScaffolder.ValueObjectSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BlueprintGenerator {
    private final FileScaffolder scaffolder = new FileScaffolder();

    public List<Path> generateFile(Path projectRoot, HiveConfig config, Path modelFile, boolean force) throws IOException {
        List<BlueprintType> types = parse(modelFile);
        List<Path> created = new ArrayList<>();
        for (BlueprintType type : types) {
            created.addAll(generate(projectRoot, config, type, force));
        }
        return created;
    }

    public List<Path> generateAll(Path projectRoot, HiveConfig config, boolean force) throws IOException {
        Path modelRoot = projectRoot.resolve(".hive/model");
        if (!Files.isDirectory(modelRoot)) {
            return List.of();
        }
        List<Path> files;
        try (var stream = Files.walk(modelRoot)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .sorted(Comparator.comparing(path -> projectRoot.relativize(path).toString()))
                    .toList();
        }
        List<Path> created = new ArrayList<>();
        for (Path file : files) {
            created.addAll(generateFile(projectRoot, config, file, force));
        }
        return created;
    }

    private List<Path> generate(Path projectRoot, HiveConfig config, BlueprintType type, boolean force) throws IOException {
        return switch (type.kind()) {
            case "id" -> scaffolder.createIdentifier(projectRoot, config, type.module(), type.name(), blankToNull(type.type()), force);
            case "vo" -> scaffolder.createValueObject(
                    projectRoot,
                    config,
                    type.module(),
                    new ValueObjectSpec(
                            type.name(),
                            blankToNull(type.type()),
                            type.notNull(),
                            type.notBlank(),
                            blankToNull(type.min()),
                            blankToNull(type.max()),
                            type.minLength(),
                            type.maxLength(),
                            blankToNull(type.pattern()),
                            type.fields(),
                            type.factory()
                    ),
                    force
            );
            case "entity" -> scaffolder.createEntity(projectRoot, config, type.module(), type.name(), type.id(), type.fields(), force);
            case "aggregate" -> scaffolder.createAggregate(projectRoot, config, type.module(), type.name(), type.id(), type.fields(), force);
            case "enum" -> scaffolder.createEnum(projectRoot, config, type.module(), type.name(), type.values(), force);
            case "event", "domainEvent" -> scaffolder.createEvent(projectRoot, config, type.module(), type.name(), type.fields(), force);
            case "exception" -> scaffolder.createException(projectRoot, config, type.module(), type.name(), blankToNull(type.message()), force);
            case "domainService", "domainservice" -> scaffolder.createDomainService(projectRoot, config, type.module(), type.name(), force);
            case "snapshot" -> scaffolder.createSnapshot(projectRoot, config, type.module(), type.name(), type.fields(), force);
            case "record" -> scaffolder.createRecord(projectRoot, config, type.module(), type.name(), type.fields(), force);
            case "class" -> scaffolder.createPlainClass(
                    projectRoot,
                    config,
                    type.module(),
                    new ClassSpec(type.name(), type.fields(), type.getters(), type.setters(), type.constructor(), type.allArgsConstructor()),
                    force
            );
            case "command" -> scaffolder.createCommand(projectRoot, config, type.module(), type.name(), type.fields(), force);
            case "outputPort", "port" -> scaffolder.createPort(projectRoot, config, type.module(), type.name(), type.methods(), force);
            case "adapter" -> scaffolder.createAdapter(projectRoot, config, type.module(), type.name(), type.ports(), force);
            default -> throw new IllegalArgumentException("Unsupported blueprint kind: " + type.kind());
        };
    }

    private List<BlueprintType> parse(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<BlueprintTypeBuilder> builders = new ArrayList<>();
        BlueprintTypeBuilder current = new BlueprintTypeBuilder();
        builders.add(current);
        String section = "";
        FieldBuilder pendingField = null;
        MethodBuilder pendingMethod = null;
        FieldBuilder pendingParameter = null;

        for (String rawLine : lines) {
            String line = stripComment(rawLine).trim();
            if (line.isBlank() || "version: 1".equals(line) || "types:".equals(line)) {
                continue;
            }
            if (line.startsWith("- kind:")) {
                if (pendingMethod != null && pendingMethod.complete()) {
                    current.methods.add(pendingMethod.render());
                    pendingMethod = null;
                }
                if (!current.isEmpty()) {
                    current = new BlueprintTypeBuilder();
                    builders.add(current);
                }
                current.kind = valueAfter(line, "- kind:");
                section = "";
                continue;
            }
            if (line.endsWith(":")) {
                section = line.substring(0, line.length() - 1);
                continue;
            }
            if (line.startsWith("kind:")) {
                current.kind = valueAfter(line, "kind:");
            } else if (line.startsWith("module:")) {
                current.module = valueAfter(line, "module:");
            } else if (line.startsWith("name:")) {
                String value = valueAfter(line, "name:");
                if ("fields".equals(section) && pendingField != null) {
                    pendingField.name = value;
                } else if ("methods".equals(section) && pendingMethod != null) {
                    pendingMethod.name = value;
                } else if ("parameters".equals(section) && pendingParameter != null) {
                    pendingParameter.name = value;
                } else if ("id".equals(section)) {
                    current.id = value;
                } else {
                    current.name = value;
                }
            } else if (line.startsWith("type:")) {
                String value = valueAfter(line, "type:");
                if ("fields".equals(section) && pendingField != null) {
                    pendingField.type = value;
                    current.fields.add(pendingField.render());
                    pendingField = null;
                } else if ("parameters".equals(section) && pendingParameter != null) {
                    pendingParameter.type = value;
                    pendingMethod.parameters.add(pendingParameter.render());
                    pendingParameter = null;
                } else if ("id".equals(section) && current.type == null) {
                    current.type = value;
                } else {
                    current.type = value;
                }
            } else if (line.startsWith("id:")) {
                current.id = valueAfter(line, "id:");
            } else if (line.startsWith("returnType:")) {
                if (pendingMethod == null) {
                    pendingMethod = new MethodBuilder();
                }
                pendingMethod.returnType = valueAfter(line, "returnType:");
            } else if (line.startsWith("- name:")) {
                if ("fields".equals(section)) {
                    pendingField = new FieldBuilder(valueAfter(line, "- name:"));
                } else if ("methods".equals(section)) {
                    if (pendingMethod != null && pendingMethod.complete()) {
                        current.methods.add(pendingMethod.render());
                    }
                    pendingMethod = new MethodBuilder();
                    pendingMethod.name = valueAfter(line, "- name:");
                } else if ("parameters".equals(section)) {
                    pendingParameter = new FieldBuilder(valueAfter(line, "- name:"));
                }
            } else if (line.startsWith("- returnType:")) {
                if (pendingMethod != null && pendingMethod.complete()) {
                    current.methods.add(pendingMethod.render());
                }
                pendingMethod = new MethodBuilder();
                pendingMethod.returnType = valueAfter(line, "- returnType:");
            } else if (line.startsWith("- ")) {
                String value = valueAfter(line, "- ");
                if ("ports".equals(section)) {
                    current.ports.add(value);
                } else if ("values".equals(section)) {
                    current.values.add(value);
                }
            } else if (line.startsWith("notBlank:")) {
                current.notBlank = Boolean.parseBoolean(valueAfter(line, "notBlank:"));
            } else if (line.startsWith("notNull:")) {
                current.notNull = Boolean.parseBoolean(valueAfter(line, "notNull:"));
            } else if (line.startsWith("min:")) {
                current.min = valueAfter(line, "min:");
            } else if (line.startsWith("max:")) {
                current.max = valueAfter(line, "max:");
            } else if (line.startsWith("minLength:")) {
                current.minLength = Integer.parseInt(valueAfter(line, "minLength:"));
            } else if (line.startsWith("maxLength:")) {
                current.maxLength = Integer.parseInt(valueAfter(line, "maxLength:"));
            } else if (line.startsWith("pattern:")) {
                current.pattern = valueAfter(line, "pattern:");
            } else if (line.startsWith("message:")) {
                current.message = valueAfter(line, "message:");
            } else if (line.startsWith("factory:")) {
                current.factory = Boolean.parseBoolean(valueAfter(line, "factory:"));
            } else if (line.startsWith("getters:")) {
                current.getters = Boolean.parseBoolean(valueAfter(line, "getters:"));
            } else if (line.startsWith("setters:")) {
                current.setters = Boolean.parseBoolean(valueAfter(line, "setters:"));
            } else if (line.startsWith("constructor:")) {
                current.constructor = Boolean.parseBoolean(valueAfter(line, "constructor:"));
            } else if (line.startsWith("allArgsConstructor:")) {
                current.allArgsConstructor = Boolean.parseBoolean(valueAfter(line, "allArgsConstructor:"));
            }

        }

        if (pendingMethod != null && pendingMethod.complete()) {
            current.methods.add(pendingMethod.render());
        }
        return builders.stream()
                .filter(builder -> !builder.isEmpty())
                .map(BlueprintTypeBuilder::build)
                .toList();
    }

    private String stripComment(String line) {
        int index = line.indexOf('#');
        return index >= 0 ? line.substring(0, index) : line;
    }

    private String valueAfter(String line, String prefix) {
        String value = line.substring(prefix.length()).trim();
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private record BlueprintType(
            String kind,
            String module,
            String name,
            String type,
            String id,
            String message,
            List<String> fields,
            List<String> methods,
            List<String> ports,
            List<String> values,
            boolean notNull,
            boolean notBlank,
            String min,
            String max,
            Integer minLength,
            Integer maxLength,
            String pattern,
            boolean factory,
            boolean getters,
            boolean setters,
            boolean constructor,
            boolean allArgsConstructor
    ) {
    }

    private static final class BlueprintTypeBuilder {
        String kind;
        String module;
        String name;
        String type;
        String id;
        String message;
        List<String> fields = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        List<String> ports = new ArrayList<>();
        List<String> values = new ArrayList<>();
        boolean notNull;
        boolean notBlank;
        String min;
        String max;
        Integer minLength;
        Integer maxLength;
        String pattern;
        boolean factory;
        boolean getters;
        boolean setters;
        boolean constructor;
        boolean allArgsConstructor;

        boolean isEmpty() {
            return kind == null && name == null;
        }

        BlueprintType build() {
            if (kind == null || kind.isBlank()) {
                throw new IllegalArgumentException("Blueprint type is missing kind");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Blueprint type is missing name");
            }
            return new BlueprintType(
                    kind,
                    module,
                    name,
                    type,
                    id,
                    message,
                    List.copyOf(fields),
                    List.copyOf(methods),
                    List.copyOf(ports),
                    List.copyOf(values),
                    notNull,
                    notBlank,
                    min,
                    max,
                    minLength,
                    maxLength,
                    pattern,
                    factory,
                    getters,
                    setters,
                    constructor,
                    allArgsConstructor
            );
        }
    }

    private static final class FieldBuilder {
        String name;
        String type;

        FieldBuilder(String name) {
            this.name = name;
        }

        String render() {
            return name + ":" + type;
        }
    }

    private static final class MethodBuilder {
        String returnType;
        String name;
        List<String> parameters = new ArrayList<>();

        boolean complete() {
            return returnType != null && name != null;
        }

        String render() {
            return returnType + " " + name + "(" + String.join(", ", parameters.stream()
                    .map(parameter -> {
                        String[] parts = parameter.split(":", 2);
                        return parts[1] + " " + parts[0];
                    })
                    .toList()) + ")";
        }
    }
}
