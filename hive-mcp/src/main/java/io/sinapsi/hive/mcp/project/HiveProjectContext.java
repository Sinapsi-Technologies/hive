package io.sinapsi.hive.mcp.project;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public final class HiveProjectContext {
    private final AtomicReference<Path> current;
    private final List<Path> allowedRoots;

    public HiveProjectContext(
            @Value("${hive.project-root:}") String initialRoot,
            @Value("${hive.allowed-roots:}") String configuredAllowedRoots) {
        this.allowedRoots = Arrays.stream(configuredAllowedRoots.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> Path.of(value).toAbsolutePath().normalize())
                .toList();
        this.current = new AtomicReference<>(initialRoot == null || initialRoot.isBlank()
                ? null
                : normalizeAndValidate(Path.of(initialRoot)));
    }

    public Path current() {
        Path selected = current.get();
        if (selected == null) {
            throw new IllegalStateException(
                    "No project root selected. Call hive_select_project before using project tools.");
        }
        return selected;
    }

    public Path currentOrNull() {
        return current.get();
    }

    public Path select(String projectRoot) {
        if (projectRoot == null || projectRoot.isBlank()) {
            throw new IllegalArgumentException("projectRoot must not be blank");
        }
        Path selected = normalizeAndValidate(Path.of(projectRoot));
        current.set(selected);
        return selected;
    }

    private Path normalizeAndValidate(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (!Files.isDirectory(normalized)) {
            throw new IllegalArgumentException("Project root must be an existing directory: " + normalized);
        }
        if (!allowedRoots.isEmpty() && allowedRoots.stream().noneMatch(normalized::startsWith)) {
            throw new IllegalArgumentException("Project root is outside the configured allowlist: " + normalized);
        }
        return normalized;
    }
}
