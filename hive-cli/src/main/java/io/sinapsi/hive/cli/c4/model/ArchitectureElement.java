package io.sinapsi.hive.cli.c4.model;

/**
 * One node in the architecture model: a stable PlantUML {@code id}, the original class
 * {@code name}, the simple {@code className} it was derived from, and its classified {@code type}.
 */
public record ArchitectureElement(String id, String name, String className, ArchitectureElementType type) {
    public String description() {
        return type.description();
    }
}
