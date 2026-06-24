package io.sinapsi.hive.cli.c4.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class C4ConfigLoaderTest {
    @TempDir
    Path tempDir;

    private final C4ConfigLoader loader = new C4ConfigLoader();

    @Test
    void returnsDefaultsWhenNoConfigFile() throws IOException {
        C4Config config = loader.load(tempDir);
        assertEquals("plantuml", config.plantumlPath());
        assertEquals("svg", config.defaultRenderFormat());
        assertFalse(config.generateSite());
        assertEquals("hive", config.theme());
    }

    @Test
    void returnsDefaultsWhenNoC4Block() throws IOException {
        Files.writeString(tempDir.resolve("hive.yml"), """
                basePackage: com.example.app
                layout: single
                """);
        C4Config config = loader.load(tempDir);
        assertEquals("plantuml", config.plantumlPath());
        assertFalse(config.generateSite());
    }

    @Test
    void readsNestedC4Block() throws IOException {
        Files.writeString(tempDir.resolve("hive.yml"), """
                basePackage: com.example.app
                layout: single
                c4:
                  plantumlPath: /opt/plantuml.jar
                  defaultRenderFormat: png
                  generateSite: true
                  theme: hive
                """);

        C4Config config = loader.load(tempDir);

        assertEquals("/opt/plantuml.jar", config.plantumlPath());
        assertEquals("png", config.defaultRenderFormat());
        assertTrue(config.generateSite());
        assertEquals("hive", config.theme());
    }

    @Test
    void stopsAtNextTopLevelKey() throws IOException {
        Files.writeString(tempDir.resolve("hive.yml"), """
                c4:
                  generateSite: true
                basePackage: com.example.app
                """);

        C4Config config = loader.load(tempDir);

        assertTrue(config.generateSite());
        // The top-level basePackage line must not be parsed as a c4 key.
        assertEquals("plantuml", config.plantumlPath());
    }
}
