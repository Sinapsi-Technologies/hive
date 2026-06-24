package io.sinapsi.hive.cli.c4.model;

import java.util.Locale;

/**
 * The C4 zoom level of a generated diagram. Hive's flagship view is {@link #COMPONENT} (C3),
 * where the hexagonal structure becomes visible; {@link #CONTAINER} and {@link #CONTEXT} are
 * higher-altitude summaries derived from the same architecture model.
 */
public enum DiagramLevel {
    CONTEXT("c1", "C1", "System Context Diagram", "C4_Context.puml"),
    CONTAINER("c2", "C2", "Container Diagram", "C4_Container.puml"),
    COMPONENT("c3", "C3", "Component Diagram", "C4_Component.puml");

    private final String filePrefix;
    private final String shortName;
    private final String titleSuffix;
    private final String c4Include;

    DiagramLevel(String filePrefix, String shortName, String titleSuffix, String c4Include) {
        this.filePrefix = filePrefix;
        this.shortName = shortName;
        this.titleSuffix = titleSuffix;
        this.c4Include = c4Include;
    }

    /** Filename prefix, e.g. {@code c3} in {@code c3-todo.puml}. */
    public String filePrefix() {
        return filePrefix;
    }

    /** Short name used in the diagram id, e.g. {@code C3} in {@code @startuml C3_Todo}. */
    public String shortName() {
        return shortName;
    }

    /** Human title suffix, e.g. {@code Component Diagram}. */
    public String titleSuffix() {
        return titleSuffix;
    }

    /** The C4-PlantUML standard library file to include for this level. */
    public String c4Include() {
        return c4Include;
    }

    /** Lower-case CLI value, e.g. {@code component}. */
    public String cliValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static DiagramLevel parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return COMPONENT;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "context", "c1" -> CONTEXT;
            case "container", "c2" -> CONTAINER;
            case "component", "c3" -> COMPONENT;
            default -> throw new IllegalArgumentException(
                    "Unknown level: " + raw + ". Use context, container, or component.");
        };
    }
}
