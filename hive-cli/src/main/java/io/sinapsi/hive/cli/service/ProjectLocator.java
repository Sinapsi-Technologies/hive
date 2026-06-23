package io.sinapsi.hive.cli.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class ProjectLocator {
    public Optional<Path> locate(Path start) {
        Path current = start.toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve(".hive-project"))) {
                return Optional.of(current);
            }
            current = current.getParent();
        }
        return Optional.empty();
    }
}
