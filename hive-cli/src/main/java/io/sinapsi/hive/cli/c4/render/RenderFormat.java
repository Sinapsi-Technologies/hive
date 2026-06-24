package io.sinapsi.hive.cli.c4.render;

import java.util.Locale;

/** The raster/vector formats PlantUML can produce from a {@code .puml} file. */
public enum RenderFormat {
    SVG("-tsvg", "svg"),
    PNG("-tpng", "png");

    private final String plantumlFlag;
    private final String extension;

    RenderFormat(String plantumlFlag, String extension) {
        this.plantumlFlag = plantumlFlag;
        this.extension = extension;
    }

    public String plantumlFlag() {
        return plantumlFlag;
    }

    public String extension() {
        return extension;
    }

    public static RenderFormat parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return SVG;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "svg" -> SVG;
            case "png" -> PNG;
            default -> throw new IllegalArgumentException("Unknown render format: " + raw + ". Use svg or png.");
        };
    }
}
