package io.sinapsi.hive.cli.c4.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Reads the optional indented {@code c4:} block from {@code hive.yml}. The toolkit keeps its YAML
 * handling intentionally tiny (no SnakeYAML dependency in the shaded jar), so this parser only
 * understands the one nested block it needs: a {@code c4:} line followed by indented
 * {@code key: value} lines.
 */
public final class C4ConfigLoader {
    public C4Config load(Path projectRoot) throws IOException {
        Path configFile = projectRoot.resolve("hive.yml");
        C4Config defaults = C4Config.defaults();
        if (!Files.exists(configFile)) {
            return defaults;
        }

        List<String> lines = Files.readAllLines(configFile);
        int start = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).strip().equals("c4:")) {
                start = i + 1;
                break;
            }
        }
        if (start < 0) {
            return defaults;
        }

        String plantumlPath = defaults.plantumlPath();
        String defaultRenderFormat = defaults.defaultRenderFormat();
        boolean generateSite = defaults.generateSite();
        String theme = defaults.theme();

        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            // The block ends at the first line that is not indented (a new top-level key).
            if (!Character.isWhitespace(line.charAt(0))) {
                break;
            }
            String trimmed = line.strip();
            if (trimmed.startsWith("#") || !trimmed.contains(":")) {
                continue;
            }
            String[] parts = trimmed.split(":", 2);
            String key = parts[0].strip();
            String value = normalize(parts[1]);
            switch (key) {
                case "plantumlPath" -> plantumlPath = value.isBlank() ? plantumlPath : value;
                case "defaultRenderFormat" -> defaultRenderFormat = value.isBlank() ? defaultRenderFormat : value;
                case "generateSite" -> generateSite = Boolean.parseBoolean(value.toLowerCase(Locale.ROOT));
                case "theme" -> theme = value.isBlank() ? theme : value;
                default -> { /* ignore unknown keys for forward compatibility */ }
            }
        }

        return new C4Config(plantumlPath, defaultRenderFormat, generateSite, theme);
    }

    private String normalize(String rawValue) {
        String value = rawValue.strip();
        boolean quoted = value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")));
        if (quoted) {
            return value.substring(1, value.length() - 1);
        }
        int comment = value.indexOf(" #");
        if (comment >= 0) {
            value = value.substring(0, comment).strip();
        }
        return value;
    }
}
