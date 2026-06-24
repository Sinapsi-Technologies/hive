package io.sinapsi.hive.cli.c4.model;

import java.util.List;

/**
 * The architecture model for a single module/bounded-context: its display {@code name}, a
 * filesystem-safe {@code slug}, the requested {@link DiagramLevel}, and the classified
 * {@code elements} and inferred {@code relations}. Both lists are pre-sorted for deterministic
 * output. A {@link io.sinapsi.hive.cli.c4.writer.DiagramWriter} turns a view into a {@code .puml}.
 */
public record ArchitectureView(
        String name,
        String slug,
        DiagramLevel level,
        List<ArchitectureElement> elements,
        List<ArchitectureRelation> relations
) {
    public ArchitectureView {
        elements = List.copyOf(elements);
        relations = List.copyOf(relations);
    }

    /** A copy of this view rendered at a different level (same underlying model). */
    public ArchitectureView withLevel(DiagramLevel newLevel) {
        return new ArchitectureView(name, slug, newLevel, elements, relations);
    }
}
