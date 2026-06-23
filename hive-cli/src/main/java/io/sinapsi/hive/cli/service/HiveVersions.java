package io.sinapsi.hive.cli.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Single source of truth for the dependency versions the CLI injects into generated poms.
 * Values are filled in at build time from {@code hive-version.properties} (Maven resource
 * filtering) and fall back to compiled-in defaults when the resource is missing or unfiltered.
 */
public final class HiveVersions {
    private static final String DEFAULT_HIVE_VERSION = "0.1.0-SNAPSHOT";
    private static final String DEFAULT_ARCHUNIT_VERSION = "1.3.0";

    private static final Properties PROPERTIES = load();

    private HiveVersions() {
    }

    public static String hive() {
        return resolve("hive.version", DEFAULT_HIVE_VERSION);
    }

    public static String archUnit() {
        return resolve("archunit.version", DEFAULT_ARCHUNIT_VERSION);
    }

    private static String resolve(String key, String fallback) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank() || value.startsWith("${")) {
            return fallback;
        }
        return value;
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = HiveVersions.class.getResourceAsStream("/hive-version.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {
            // fall back to compiled-in defaults
        }
        return properties;
    }
}
