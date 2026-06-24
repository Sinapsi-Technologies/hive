package io.sinapsi.hive.cli.c4.render;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Outcome of a render attempt. Either a diagram was produced ({@link #rendered}) with its
 * {@link #output} path, or it was skipped with a human-readable {@link #skipReason}. Rendering
 * never throws for "PlantUML not installed" — it degrades to a skip so {@code .puml} output is
 * always kept.
 */
public record RenderResult(boolean rendered, Path output, String skipReason) {
    public static RenderResult rendered(Path output) {
        return new RenderResult(true, output, null);
    }

    public static RenderResult skipped(String reason) {
        return new RenderResult(false, null, reason);
    }

    public Optional<Path> outputPath() {
        return Optional.ofNullable(output);
    }
}
