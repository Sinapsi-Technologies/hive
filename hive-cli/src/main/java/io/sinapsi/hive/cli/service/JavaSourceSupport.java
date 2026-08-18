package io.sinapsi.hive.cli.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class JavaSourceSupport {
    private static final Set<String> JAVA_LANG = Set.of(
            "String", "Integer", "Long", "Boolean", "Double", "Float", "Short", "Byte", "Character",
            "Object", "Void"
    );
    private static final Set<String> PRIMITIVES = Set.of(
            "void", "boolean", "byte", "short", "int", "long", "float", "double", "char"
    );

    private final NameResolver names = new NameResolver();

    public List<FieldSpec> parseFields(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().map(this::parseField).toList();
    }

    public FieldSpec parseField(String value) {
        int separator = value == null ? -1 : value.indexOf(':');
        if (separator <= 0 || separator == value.length() - 1) {
            throw new IllegalArgumentException("Field must use name:Type syntax: " + value);
        }
        String fieldName = value.substring(0, separator).trim();
        String type = normalizeType(value.substring(separator + 1).trim());
        requireJavaIdentifier(fieldName, "field name");
        requireType(type);
        return new FieldSpec(fieldName, type);
    }

    public List<MethodSpec> parseMethods(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().map(this::parseMethod).toList();
    }

    public MethodSpec parseMethod(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Method signature must not be blank");
        }
        String signature = value.trim();
        int open = signature.indexOf('(');
        int close = signature.lastIndexOf(')');
        if (open <= 0 || close != signature.length() - 1) {
            throw new IllegalArgumentException("Unsupported method signature: " + value);
        }

        String beforeParameters = signature.substring(0, open).trim();
        int nameStart = beforeParameters.length() - 1;
        while (nameStart >= 0 && Character.isJavaIdentifierPart(beforeParameters.charAt(nameStart))) {
            nameStart--;
        }
        String methodName = beforeParameters.substring(nameStart + 1);
        String returnType = beforeParameters.substring(0, nameStart + 1).trim();
        if (methodName.isBlank() || returnType.isBlank()) {
            throw new IllegalArgumentException("Unsupported method signature: " + value);
        }
        requireJavaIdentifier(methodName, "method name");
        requireType(returnType);

        String rawParameters = signature.substring(open + 1, close).trim();
        List<FieldSpec> parameters = new ArrayList<>();
        if (!rawParameters.isBlank()) {
            for (String rawParameter : splitTopLevel(rawParameters, ',')) {
                String parameter = rawParameter.trim();
                int space = lastTopLevelSpace(parameter);
                if (space <= 0 || space == parameter.length() - 1) {
                    throw new IllegalArgumentException("Unsupported method parameter: " + rawParameter);
                }
                String type = normalizeType(parameter.substring(0, space).trim());
                String name = parameter.substring(space + 1).trim();
                requireType(type);
                requireJavaIdentifier(name, "parameter name");
                parameters.add(new FieldSpec(name, type));
            }
        }

        return new MethodSpec(returnType, methodName, List.copyOf(parameters));
    }

    public String importsFor(List<String> types) {
        Set<String> imports = new TreeSet<>();
        for (String type : types) {
            collectImports(type, imports);
        }
        if (imports.isEmpty()) {
            return "";
        }
        StringBuilder rendered = new StringBuilder();
        for (String importName : imports) {
            rendered.append("import ").append(importName).append(";\n");
        }
        return rendered.append("\n").toString();
    }

    public List<String> referencedTypes(String type) {
        List<String> found = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < type.length(); i++) {
            char c = type.charAt(i);
            if (Character.isJavaIdentifierPart(c) || c == '.') {
                token.append(c);
            } else {
                flushTypeToken(token, found);
            }
        }
        flushTypeToken(token, found);
        return found;
    }

    public void requireType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type must not be blank");
        }
        int depth = 0;
        for (int i = 0; i < type.length(); i++) {
            char c = type.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
                if (depth < 0) {
                    throw new IllegalArgumentException("Invalid generic type: " + type);
                }
            } else if (!(Character.isJavaIdentifierPart(c) || c == '.' || c == ',' || c == '?' || Character.isWhitespace(c))) {
                throw new IllegalArgumentException("Unsupported type syntax: " + type);
            }
        }
        if (depth != 0) {
            throw new IllegalArgumentException("Invalid generic type: " + type);
        }
        for (String referencedType : referencedTypes(type)) {
            if (PRIMITIVES.contains(referencedType)) {
                continue;
            }
            String simpleName = simpleName(referencedType);
            names.requireJavaTypeName(simpleName);
        }
    }

    public String simpleName(String typeName) {
        int index = typeName.lastIndexOf('.');
        return index >= 0 ? typeName.substring(index + 1) : typeName;
    }

    private void collectImports(String type, Set<String> imports) {
        for (String referencedType : referencedTypes(type)) {
            if (referencedType.contains(".")) {
                if (!referencedType.startsWith("java.lang.")) {
                    imports.add(referencedType);
                }
                continue;
            }
            if (JAVA_LANG.contains(referencedType) || PRIMITIVES.contains(referencedType)) {
                continue;
            }
            switch (referencedType) {
                case "BigDecimal" -> imports.add("java.math.BigDecimal");
                case "BigInteger" -> imports.add("java.math.BigInteger");
                case "Instant" -> imports.add("java.time.Instant");
                case "LocalDate" -> imports.add("java.time.LocalDate");
                case "LocalDateTime" -> imports.add("java.time.LocalDateTime");
                case "OffsetDateTime" -> imports.add("java.time.OffsetDateTime");
                case "ZonedDateTime" -> imports.add("java.time.ZonedDateTime");
                case "Collection" -> imports.add("java.util.Collection");
                case "List" -> imports.add("java.util.List");
                case "Map" -> imports.add("java.util.Map");
                case "Optional" -> imports.add("java.util.Optional");
                case "Set" -> imports.add("java.util.Set");
                case "UUID" -> imports.add("java.util.UUID");
                default -> {
                    // Project-local types live in neighbouring packages by convention; callers add those imports.
                }
            }
        }
    }

    private List<String> splitTopLevel(String value, char delimiter) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            } else if (c == delimiter && depth == 0) {
                parts.add(value.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(value.substring(start));
        return parts;
    }

    private int lastTopLevelSpace(String value) {
        int depth = 0;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c == '>') {
                depth++;
            } else if (c == '<') {
                depth--;
            } else if (Character.isWhitespace(c) && depth == 0) {
                return i;
            }
        }
        return -1;
    }

    private void requireJavaIdentifier(String value, String label) {
        if (value == null || value.isBlank() || !Character.isJavaIdentifierStart(value.charAt(0))) {
            throw new IllegalArgumentException("Invalid Java " + label + ": " + value);
        }
        for (int i = 1; i < value.length(); i++) {
            if (!Character.isJavaIdentifierPart(value.charAt(i))) {
                throw new IllegalArgumentException("Invalid Java " + label + ": " + value);
            }
        }
    }

    private String normalizeType(String type) {
        StringBuilder normalized = new StringBuilder();
        boolean previousWhitespace = false;
        for (int i = 0; i < type.length(); i++) {
            char c = type.charAt(i);
            if (Character.isWhitespace(c)) {
                previousWhitespace = true;
                continue;
            }
            if (c == ',' && normalized.length() > 0) {
                normalized.append(", ");
                previousWhitespace = false;
            } else {
                if (previousWhitespace && normalized.length() > 0 && Character.isJavaIdentifierPart(c)
                        && Character.isJavaIdentifierPart(normalized.charAt(normalized.length() - 1))) {
                    normalized.append(' ');
                }
                normalized.append(c);
                previousWhitespace = false;
            }
        }
        return normalized.toString();
    }

    private void flushTypeToken(StringBuilder token, List<String> found) {
        if (token.isEmpty()) {
            return;
        }
        String value = token.toString();
        if (!"?".equals(value)) {
            found.add(value);
        }
        token.setLength(0);
    }
}
