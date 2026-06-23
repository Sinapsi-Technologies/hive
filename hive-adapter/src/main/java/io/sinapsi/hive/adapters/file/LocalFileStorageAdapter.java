package io.sinapsi.hive.adapters.file;

import io.sinapsi.hive.ports.FileStoragePort;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class LocalFileStorageAdapter implements FileStoragePort {

    private final Path root;

    public LocalFileStorageAdapter(Path root) {
        this.root = Objects.requireNonNull(root, "root must not be null")
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public void save(String path, byte[] content) {
        Objects.requireNonNull(content, "content must not be null");

        Path target = resolveInsideRoot(path);
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(target, content);
        } catch (java.io.IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @Override
    public byte[] load(String path) {
        Path target = resolveInsideRoot(path);
        try {
            return Files.readAllBytes(target);
        } catch (java.io.IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private Path resolveInsideRoot(String path) {
        Objects.requireNonNull(path, "path must not be null");

        Path resolved = root.resolve(path).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("path must stay inside storage root");
        }
        return resolved;
    }
}
