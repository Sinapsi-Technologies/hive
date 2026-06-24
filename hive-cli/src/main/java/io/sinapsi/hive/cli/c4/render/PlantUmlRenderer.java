package io.sinapsi.hive.cli.c4.render;

import io.sinapsi.hive.cli.c4.config.C4Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Renders {@code .puml} files to SVG/PNG by shelling out to PlantUML — either a {@code plantuml}
 * executable on the {@code PATH} or {@code java -jar plantuml.jar} when {@code c4.plantumlPath}
 * points at a jar. If no renderer is available it degrades to a {@link RenderResult#skipped}
 * result with an actionable message; it never throws for a missing renderer.
 */
public final class PlantUmlRenderer {
    private static final long TIMEOUT_SECONDS = 120;

    private final List<String> baseCommand;

    public PlantUmlRenderer(C4Config config) {
        this(toBaseCommand(config.plantumlPath()));
    }

    /** Visible for testing: inject the exact base command (e.g. a bogus one to assert fallback). */
    public PlantUmlRenderer(List<String> baseCommand) {
        this.baseCommand = List.copyOf(baseCommand);
    }

    static List<String> toBaseCommand(String plantumlPath) {
        String path = plantumlPath == null || plantumlPath.isBlank() ? "plantuml" : plantumlPath.trim();
        if (path.toLowerCase(Locale.ROOT).endsWith(".jar")) {
            return List.of("java", "-jar", path);
        }
        return List.of(path);
    }

    /** Whether the configured PlantUML can be invoked (runs {@code <cmd> -version}). */
    public boolean isAvailable() {
        try {
            List<String> command = new ArrayList<>(baseCommand);
            command.add("-version");
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    public RenderResult render(Path pumlFile, RenderFormat format) {
        if (!isAvailable()) {
            return RenderResult.skipped(
                    "Rendering skipped: PlantUML executable not found. "
                            + "Install PlantUML or configure c4.plantumlPath in hive.yml.");
        }
        try {
            List<String> command = new ArrayList<>(baseCommand);
            command.add(format.plantumlFlag());
            command.add(pumlFile.getFileName().toString());
            Process process = new ProcessBuilder(command)
                    .directory(pumlFile.toAbsolutePath().getParent().toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return RenderResult.skipped("Rendering skipped: PlantUML timed out.");
            }
            if (process.exitValue() != 0) {
                return RenderResult.skipped("Rendering skipped: PlantUML exited with code " + process.exitValue() + ".");
            }
            Path output = outputPath(pumlFile, format);
            if (Files.exists(output)) {
                return RenderResult.rendered(output);
            }
            return RenderResult.skipped("Rendering skipped: PlantUML did not produce " + output.getFileName() + ".");
        } catch (IOException exception) {
            return RenderResult.skipped("Rendering skipped: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return RenderResult.skipped("Rendering skipped: interrupted.");
        }
    }

    private Path outputPath(Path pumlFile, RenderFormat format) {
        String fileName = pumlFile.getFileName().toString();
        String base = fileName.endsWith(".puml") ? fileName.substring(0, fileName.length() - ".puml".length()) : fileName;
        return pumlFile.toAbsolutePath().getParent().resolve(base + "." + format.extension());
    }
}
