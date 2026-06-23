package io.sinapsi.hive.adapters.file;

import io.sinapsi.hive.core.port.OutputPort;
import io.sinapsi.hive.ports.FileStoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalFileStorageAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void implementsFileStoragePortAndOutputPort() {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(tempDir);

        assertInstanceOf(FileStoragePort.class, adapter);
        assertInstanceOf(OutputPort.class, adapter);
    }

    @Test
    void savesAndLoadsBytesInsideStorageRoot() throws Exception {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(tempDir);
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);

        adapter.save("docs/readme.txt", content);

        assertArrayEquals(content, adapter.load("docs/readme.txt"));
        assertEquals("hello", Files.readString(tempDir.resolve("docs/readme.txt")));
    }

    @Test
    void createsParentDirectoriesWhenSaving() {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(tempDir);

        adapter.save("a/b/c/file.txt", "content".getBytes(StandardCharsets.UTF_8));

        assertArrayEquals(
                "content".getBytes(StandardCharsets.UTF_8),
                adapter.load("a/b/c/file.txt")
        );
    }

    @Test
    void normalizesRelativeSegmentsInsideStorageRoot() {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(tempDir);

        adapter.save("docs/../docs/readme.txt", "content".getBytes(StandardCharsets.UTF_8));

        assertArrayEquals(
                "content".getBytes(StandardCharsets.UTF_8),
                adapter.load("docs/readme.txt")
        );
    }

    @Test
    void rejectsPathTraversalOutsideStorageRoot() {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(tempDir);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.save("../outside.txt", new byte[]{1})
        );

        assertEquals("path must stay inside storage root", exception.getMessage());
        assertFalse(Files.exists(tempDir.resolveSibling("outside.txt")));
    }

    @Test
    void rejectsNullRoot() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new LocalFileStorageAdapter(null)
        );

        assertEquals("root must not be null", exception.getMessage());
    }

    @Test
    void rejectsNullPath() {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(tempDir);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> adapter.load(null)
        );

        assertEquals("path must not be null", exception.getMessage());
    }

    @Test
    void rejectsNullContentWhenSaving() {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(tempDir);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> adapter.save("file.txt", null)
        );

        assertEquals("content must not be null", exception.getMessage());
    }

    @Test
    void wrapsIoFailureWhenLoadingMissingFile() {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(tempDir);

        assertThrows(UncheckedIOException.class, () -> adapter.load("missing.txt"));
    }
}
