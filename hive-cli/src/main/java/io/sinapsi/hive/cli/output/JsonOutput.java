package io.sinapsi.hive.cli.output;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON serializer for the CLI's {@code --json} output. Kept dependency-free so the
 * shaded CLI jar stays small. Handles the shapes the commands emit: strings, booleans, numbers,
 * nested objects ({@link Map}), and arrays ({@link List}) of any of these. Machine-readable
 * output lets an agent drive the CLI and learn the exact files it created without parsing prose.
 */
public final class JsonOutput {
    private static final String INDENT = "  ";

    private JsonOutput() {
    }

    public static String render(Map<String, Object> fields) {
        StringBuilder json = new StringBuilder();
        writeObject(json, fields, 0);
        return json.toString();
    }

    public static List<String> relativePaths(Path root, List<Path> paths) {
        return paths.stream().map(path -> relativize(root, path)).toList();
    }

    public static String relativePath(Path root, Path path) {
        return relativize(root, path);
    }

    private static void writeObject(StringBuilder json, Map<?, ?> map, int depth) {
        if (map.isEmpty()) {
            json.append("{}");
            return;
        }
        json.append("{");
        String inner = INDENT.repeat(depth + 1);
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            json.append("\n").append(inner).append(quote(String.valueOf(entry.getKey()))).append(": ");
            writeValue(json, entry.getValue(), depth + 1);
        }
        json.append("\n").append(INDENT.repeat(depth)).append("}");
    }

    private static void writeArray(StringBuilder json, List<?> list, int depth) {
        if (list.isEmpty()) {
            json.append("[]");
            return;
        }
        json.append("[");
        String inner = INDENT.repeat(depth + 1);
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\n").append(inner);
            writeValue(json, list.get(i), depth + 1);
        }
        json.append("\n").append(INDENT.repeat(depth)).append("]");
    }

    private static void writeValue(StringBuilder json, Object value, int depth) {
        if (value == null) {
            json.append("null");
        } else if (value instanceof Boolean bool) {
            json.append(bool);
        } else if (value instanceof Number number) {
            json.append(number);
        } else if (value instanceof Map<?, ?> map) {
            writeObject(json, map, depth);
        } else if (value instanceof List<?> list) {
            writeArray(json, list, depth);
        } else {
            json.append(quote(String.valueOf(value)));
        }
    }

    private static String relativize(Path root, Path path) {
        Path normalizedPath = path.toAbsolutePath().normalize();
        Path relative;
        try {
            relative = root.toAbsolutePath().normalize().relativize(normalizedPath);
        } catch (IllegalArgumentException exception) {
            relative = normalizedPath;
        }
        // When the path is outside the project (e.g. an out-of-tree --output), a chain of "../"
        // is noise; show the absolute path instead.
        if (relative.startsWith("..")) {
            relative = normalizedPath;
        }
        return relative.toString().replace(File.separatorChar, '/');
    }

    private static String quote(String raw) {
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.append("\"").toString();
    }
}
