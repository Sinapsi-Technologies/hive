package io.sinapsi.hive.cli.c4.writer;

import io.sinapsi.hive.cli.c4.model.ArchitectureView;

/**
 * Renders an {@link ArchitectureView} into the text of a diagram file. Implementations are pure
 * (no I/O), so the command owns the deterministic overwrite rules. The MVP ships
 * {@link PlantUmlC4Writer}; the abstraction leaves room for {@code mermaid}/{@code structurizr}.
 */
public interface DiagramWriter {
    /** The output format identifier, e.g. {@code puml}. */
    String format();

    /** The file name for this view, e.g. {@code c3-todo.puml}. */
    String fileName(ArchitectureView view);

    /** The full diagram source for this view. */
    String render(ArchitectureView view);
}
