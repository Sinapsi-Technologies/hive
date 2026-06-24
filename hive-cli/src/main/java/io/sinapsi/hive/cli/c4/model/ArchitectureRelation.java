package io.sinapsi.hive.cli.c4.model;

/**
 * A directed relation between two {@link ArchitectureElement}s, identified by their ids, with a
 * short verb describing the link ({@code calls}, {@code uses}, {@code implements}, ...).
 */
public record ArchitectureRelation(String sourceId, String targetId, String description) {
}
