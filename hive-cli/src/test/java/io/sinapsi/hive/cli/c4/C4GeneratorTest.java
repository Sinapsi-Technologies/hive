package io.sinapsi.hive.cli.c4;

import io.sinapsi.hive.cli.c4.config.C4Config;
import io.sinapsi.hive.cli.c4.model.DiagramLevel;
import io.sinapsi.hive.cli.c4.render.PlantUmlRenderer;
import io.sinapsi.hive.cli.c4.render.RenderFormat;
import io.sinapsi.hive.cli.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class C4GeneratorTest {
    @TempDir
    Path tempDir;

    private final HiveConfig config = new HiveConfig("com.example.app", "modular", "src/main/java", "src/test/java");

    private C4Generator.Request request(boolean render, boolean site, boolean force) {
        return new C4Generator.Request(
                tempDir,
                config,
                C4Config.defaults(),
                "todo",
                DiagramLevel.COMPONENT,
                tempDir.resolve("docs/architecture"),
                render,
                render ? RenderFormat.SVG : null,
                site,
                force);
    }

    private boolean createdEndsWith(C4Generator.Result result, String suffix) {
        return result.created().stream().anyMatch(path -> path.getFileName().toString().equals(suffix));
    }

    @Test
    void generatesPumlForTheModule() throws IOException {
        C4TestProject.todoModule(tempDir);

        C4Generator.Result result = new C4Generator().generate(request(false, false, false));

        assertTrue(createdEndsWith(result, "c3-todo.puml"));
        assertTrue(Files.exists(tempDir.resolve("docs/architecture/c3-todo.puml")));
        assertEquals(1, result.views().size());
        assertEquals(6, result.views().getFirst().elements().size());
        assertTrue(result.skipped().isEmpty());
    }

    @Test
    void generatesSitePage() throws IOException {
        C4TestProject.todoModule(tempDir);

        C4Generator.Result result = new C4Generator().generate(request(false, true, false));

        assertTrue(createdEndsWith(result, "index.html"));
        assertTrue(Files.exists(tempDir.resolve("docs/architecture/index.html")));
        assertTrue(Files.readString(tempDir.resolve("docs/architecture/index.html")).contains("Architecture Map"));
    }

    @Test
    void doesNotOverwriteWithoutForce() throws IOException {
        C4TestProject.todoModule(tempDir);
        new C4Generator().generate(request(false, false, false));

        C4Generator.Result second = new C4Generator().generate(request(false, false, false));

        assertFalse(createdEndsWith(second, "c3-todo.puml"));
        assertTrue(second.skipped().stream().anyMatch(p -> p.getFileName().toString().equals("c3-todo.puml")));
        assertTrue(second.warnings().stream().anyMatch(w -> w.contains("already exists")));
    }

    @Test
    void overwritesWithForce() throws IOException {
        C4TestProject.todoModule(tempDir);
        new C4Generator().generate(request(false, false, false));

        C4Generator.Result forced = new C4Generator().generate(request(false, false, true));

        assertTrue(createdEndsWith(forced, "c3-todo.puml"));
        assertTrue(forced.skipped().isEmpty());
    }

    @Test
    void degradesGracefullyWhenRendererUnavailable() throws IOException {
        C4TestProject.todoModule(tempDir);
        PlantUmlRenderer bogus = new PlantUmlRenderer(List.of("hive-no-such-plantuml-binary"));

        C4Generator.Result result = new C4Generator(bogus).generate(request(true, false, false));

        assertTrue(createdEndsWith(result, "c3-todo.puml"), "puml must still be generated");
        assertFalse(createdEndsWith(result, "c3-todo.svg"), "svg must not be created when renderer is missing");
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("PlantUML")), result.warnings().toString());
    }

    @Test
    void warnsWhenModuleNotFound() throws IOException {
        C4TestProject.todoModule(tempDir);
        C4Generator.Request request = new C4Generator.Request(
                tempDir, config, C4Config.defaults(), "missing", DiagramLevel.COMPONENT,
                tempDir.resolve("docs/architecture"), false, null, false, false);

        C4Generator.Result result = new C4Generator().generate(request);

        assertTrue(result.views().isEmpty());
        assertTrue(result.created().isEmpty());
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("missing")), result.warnings().toString());
    }
}
