package io.sinapsi.hive.cli.c4.config;

/**
 * Optional {@code c4:} configuration read from {@code hive.yml}:
 *
 * <pre>
 * c4:
 *   plantumlPath: plantuml
 *   defaultRenderFormat: svg
 *   generateSite: true
 *   theme: hive
 * </pre>
 *
 * Every field has a sensible default, so the section is entirely optional.
 */
public record C4Config(
        String plantumlPath,
        String defaultRenderFormat,
        boolean generateSite,
        String theme
) {
    public static C4Config defaults() {
        return new C4Config("plantuml", "svg", false, "hive");
    }
}
