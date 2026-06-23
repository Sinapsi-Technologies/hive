package io.sinapsi.hive.cli.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectLocatorTest {
    @TempDir
    Path tempDir;

    @Test
    void locatesProjectFromNestedDirectory() throws Exception {
        Files.writeString(tempDir.resolve(".hive-project"), "hive-toolkit\n");
        Path nested = Files.createDirectories(tempDir.resolve("src/main/java"));

        assertEquals(tempDir, new ProjectLocator().locate(nested).orElseThrow());
    }
}
