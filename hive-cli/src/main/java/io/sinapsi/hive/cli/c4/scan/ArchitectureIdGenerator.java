package io.sinapsi.hive.cli.c4.scan;

import java.util.HashSet;
import java.util.Set;

/**
 * Generates stable, lower-camel-case PlantUML ids from class names
 * ({@code TodoController -> todoController}, {@code InMemoryTodoListRepositoryAdapter ->
 * inMemoryTodoListRepositoryAdapter}). Invalid id characters are stripped and collisions are
 * resolved with a numeric suffix. Feed names in a deterministic order and ids stay stable across
 * runs.
 */
public final class ArchitectureIdGenerator {
    private final Set<String> used = new HashSet<>();

    public String generate(String className) {
        String sanitized = sanitize(className);
        if (sanitized.isEmpty()) {
            sanitized = "element";
        }
        String candidate = sanitized;
        int suffix = 2;
        while (!used.add(candidate)) {
            candidate = sanitized + suffix;
            suffix++;
        }
        return candidate;
    }

    private String sanitize(String className) {
        StringBuilder builder = new StringBuilder(className.length());
        for (int i = 0; i < className.length(); i++) {
            char c = className.charAt(i);
            if (c == '_' || Character.isLetterOrDigit(c)) {
                builder.append(c);
            }
        }
        if (builder.isEmpty()) {
            return "";
        }
        builder.setCharAt(0, Character.toLowerCase(builder.charAt(0)));
        return builder.toString();
    }
}
