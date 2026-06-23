package io.sinapsi.hive.cli.service;

import io.sinapsi.hive.cli.model.HiveConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HiveConfigLoader {
    public HiveConfig load(Path projectRoot) throws IOException {
        Path configFile = projectRoot.resolve("hive.yml");
        HiveConfig defaults = HiveConfig.defaults();
        if (!Files.exists(configFile)) {
            return defaults;
        }

        Map<String, String> values = new LinkedHashMap<>();
        for (String line : Files.readAllLines(configFile)) {
            String trimmed = line.trim();
            if (trimmed.isBlank() || trimmed.startsWith("#") || !trimmed.contains(":")) {
                continue;
            }
            String[] parts = trimmed.split(":", 2);
            String key = parts[0].trim();
            String value = normalizeValue(parts[1]);
            if (!key.isBlank()) {
                values.put(key, value);
            }
        }

        return new HiveConfig(
                values.getOrDefault("basePackage", defaults.basePackage()),
                values.getOrDefault("layout", defaults.layout()),
                values.getOrDefault("javaSourceRoot", defaults.javaSourceRoot()),
                values.getOrDefault("testSourceRoot", defaults.testSourceRoot())
        );
    }

    private String normalizeValue(String rawValue) {
        String value = rawValue.trim();

        boolean quoted = (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2)
                || (value.startsWith("'") && value.endsWith("'") && value.length() >= 2);
        if (quoted) {
            return value.substring(1, value.length() - 1);
        }

        // strip a trailing inline comment (only for unquoted values)
        int comment = value.indexOf(" #");
        if (comment >= 0) {
            value = value.substring(0, comment).trim();
        }
        return value;
    }

    public String render(HiveConfig config) {
        return """
                basePackage: %s
                layout: %s
                javaSourceRoot: %s
                testSourceRoot: %s
                """.formatted(
                config.basePackage(),
                config.layout(),
                config.javaSourceRoot(),
                config.testSourceRoot()
        );
    }
}
