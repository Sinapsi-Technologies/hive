package io.sinapsi.hive.cli.output;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON serializer for the CLI's {@code --json} output. Kept dependency-free so the
 * shaded CLI jar stays small; only handles the shapes the commands emit (strings, booleans,
 * and string arrays). Machine-readable output lets an agent drive the CLI and learn the exact
 * files it created without parsing prose.
 */
public final class JsonOutput {
    private JsonOutput() {
    }

    public static String render(Map<String, Object> fields) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            json.append("\n  ").append(quote(entry.getKey())).append(": ").append(value(entry.getValue()));
        }
        json.append(fields.isEmpty() ? "}" : "\n}");
        return json.toString();
    }

    public static List<String> relativePaths(Path root, List<Path> paths) {
        return paths.stream().map(path -> relativize(root, path)).toList();
    }

    private static String relativize(Path root, Path path) {
        Path relative;
        try {
            relative = root.toAbsolutePath().normalize().relativize(path.toAbsolutePath().normalize());
        } catch (IllegalArgumentException exception) {
            relative = path;
        }
        return relative.toString().replace(File.separatorChar, '/');
    }

    private static String value(Object value) {
        if (value instanceof Boolean bool) {
            return bool.toString();
        }
        if (value instanceof List<?> list) {
            StringBuilder array = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    array.append(",");
                }
                array.append("\n    ").append(quote(String.valueOf(list.get(i))));
            }
            array.append(list.isEmpty() ? "]" : "\n  ]");
            return array.toString();
        }
        return quote(String.valueOf(value));
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
