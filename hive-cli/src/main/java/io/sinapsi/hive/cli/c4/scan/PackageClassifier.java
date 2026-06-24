package io.sinapsi.hive.cli.c4.scan;

import io.sinapsi.hive.cli.c4.model.ArchitectureElementType;

/**
 * Maps a Java package name to an {@link ArchitectureElementType} using the Hive package
 * convention. Matching is segment-aware (the package is wrapped in dots so {@code .domain.}
 * never matches a word like {@code domainservice}) and ordered most-specific-first.
 */
public final class PackageClassifier {
    /**
     * Classifies using both the package and the class name. The name refines a few cases the
     * package alone gets wrong — most importantly, result/DTO records that live in the
     * {@code application.services} package are not application services (those are named
     * {@code *Service}), so they are not drawn as components.
     */
    public ArchitectureElementType classify(String packageName, String className) {
        ArchitectureElementType type = classify(packageName);
        if (type == ArchitectureElementType.APPLICATION_SERVICE
                && className != null
                && !className.endsWith("Service")) {
            return ArchitectureElementType.UNKNOWN;
        }
        return type;
    }

    public ArchitectureElementType classify(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            return ArchitectureElementType.UNKNOWN;
        }
        String p = "." + packageName + ".";

        if (contains(p, "application.ports.in.commands")) {
            return ArchitectureElementType.COMMAND;
        }
        if (contains(p, "application.ports.in")) {
            return ArchitectureElementType.INPUT_PORT;
        }
        if (contains(p, "application.ports.out")) {
            return ArchitectureElementType.OUTPUT_PORT;
        }
        if (contains(p, "application.services")) {
            return ArchitectureElementType.APPLICATION_SERVICE;
        }
        if (contains(p, "domain.aggregates")) {
            return ArchitectureElementType.DOMAIN_AGGREGATE;
        }
        if (contains(p, "domain.entities")) {
            return ArchitectureElementType.DOMAIN_ENTITY;
        }
        if (contains(p, "domain.valueobjects")) {
            return ArchitectureElementType.DOMAIN_VALUE_OBJECT;
        }
        if (contains(p, "domain.events")) {
            return ArchitectureElementType.DOMAIN_EVENT;
        }
        if (contains(p, "domain.services")) {
            return ArchitectureElementType.DOMAIN_SERVICE;
        }
        if (contains(p, "domain.exceptions")) {
            return ArchitectureElementType.DOMAIN_EXCEPTION;
        }
        if (contains(p, "domain.snapshots")) {
            return ArchitectureElementType.DOMAIN_SNAPSHOT;
        }
        if (contains(p, "domain")) {
            return ArchitectureElementType.DOMAIN_MODEL;
        }
        if (contains(p, "infrastructure.adapters.in")) {
            return ArchitectureElementType.INBOUND_ADAPTER;
        }
        if (contains(p, "infrastructure.adapters.out")) {
            return ArchitectureElementType.OUTBOUND_ADAPTER;
        }
        if (contains(p, "infrastructure.configs")) {
            return ArchitectureElementType.CONFIGURATION;
        }
        if (contains(p, "configurations")) {
            return ArchitectureElementType.CONFIGURATION;
        }
        if (contains(p, "commons")) {
            return ArchitectureElementType.COMMON;
        }
        return ArchitectureElementType.UNKNOWN;
    }

    private boolean contains(String dottedPackage, String segments) {
        return dottedPackage.contains("." + segments + ".");
    }
}
