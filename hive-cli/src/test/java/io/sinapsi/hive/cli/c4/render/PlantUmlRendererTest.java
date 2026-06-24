package io.sinapsi.hive.cli.c4.render;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantUmlRendererTest {
    @TempDir
    Path tempDir;

    @Test
    void reportsUnavailableForBogusExecutable() {
        PlantUmlRenderer renderer = new PlantUmlRenderer(List.of("hive-no-such-plantuml-binary"));
        assertFalse(renderer.isAvailable());
    }

    @Test
    void degradesGracefullyWhenRendererMissing() throws IOException {
        Path puml = tempDir.resolve("c3-todo.puml");
        Files.writeString(puml, "@startuml\n@enduml\n");

        PlantUmlRenderer renderer = new PlantUmlRenderer(List.of("hive-no-such-plantuml-binary"));
        RenderResult result = renderer.render(puml, RenderFormat.SVG);

        assertFalse(result.rendered());
        assertTrue(result.skipReason().contains("PlantUML"), result.skipReason());
        assertTrue(Files.exists(puml), "The .puml file must be preserved");
        assertFalse(Files.exists(tempDir.resolve("c3-todo.svg")));
    }

    @Test
    void mapsConfiguredJarToJavaCommand() {
        assertEquals(List.of("java", "-jar", "tools/plantuml.jar"),
                PlantUmlRenderer.toBaseCommand("tools/plantuml.jar"));
        assertEquals(List.of("plantuml"), PlantUmlRenderer.toBaseCommand("plantuml"));
        assertEquals(List.of("plantuml"), PlantUmlRenderer.toBaseCommand(""));
    }

    @Test
    void renderFormatParsesValuesAndRejectsUnknown() {
        assertEquals(RenderFormat.SVG, RenderFormat.parse(null));
        assertEquals(RenderFormat.SVG, RenderFormat.parse("svg"));
        assertEquals(RenderFormat.PNG, RenderFormat.parse("PNG"));
        try {
            RenderFormat.parse("gif");
            org.junit.jupiter.api.Assertions.fail("Expected rejection of unknown format");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("gif"));
        }
    }
}
