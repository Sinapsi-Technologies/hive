package io.sinapsi.hive.cli.service.blueprint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BlueprintParser {
    public List<BlueprintType> parse(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<BlueprintTypeBuilder> builders = new ArrayList<>();
        BlueprintTypeBuilder current = new BlueprintTypeBuilder();
        builders.add(current);
        String section = "";
        FieldBuilder pendingField = null;
        MethodBuilder pendingMethod = null;
        FieldBuilder pendingParameter = null;

        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String rawLine = lines.get(lineNumber - 1);
            String line = stripComment(rawLine).trim();
            if (line.isBlank() || "version: 1".equals(line) || "types:".equals(line)) {
                continue;
            }
            if (line.startsWith("types:")) {
                throw new BlueprintParseException("Invalid types declaration. Expected 'types:'", lineNumber);
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
                current.minLength = parseInteger(valueAfter(line, "minLength:"), lineNumber);
            } else if (line.startsWith("maxLength:")) {
                current.maxLength = parseInteger(valueAfter(line, "maxLength:"), lineNumber);
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

    private Integer parseInteger(String value, int lineNumber) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new BlueprintParseException("Expected integer value but found '" + value + "'", lineNumber);
        }
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
                throw new BlueprintParseException("Blueprint type is missing kind", null);
            }
            if (name == null || name.isBlank()) {
                throw new BlueprintParseException("Blueprint type is missing name", null);
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
