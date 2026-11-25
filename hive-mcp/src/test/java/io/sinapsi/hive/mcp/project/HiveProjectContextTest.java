package io.sinapsi.hive.mcp.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HiveProjectContextTest {

    @Test
    void requiresExplicitProjectSelectionWhenNoInitialRootIsConfigured() {
        HiveProjectContext context = new HiveProjectContext("", "");

        assertThrows(IllegalStateException.class, context::current);
    }

    @Test
    void selectsAndNormalizesProjectRoot(@TempDir Path project) {
        HiveProjectContext context = new HiveProjectContext("", "");

        assertEquals(project.toAbsolutePath().normalize(), context.select(project.toString()));
        assertEquals(project.toAbsolutePath().normalize(), context.current());
    }

    @Test
    void reportsUnselectedProjectWithoutThrowing() {
        HiveProjectContext context = new HiveProjectContext("", "");
        var result = new HiveProjectContextTools(context).currentProject();

        assertFalse((Boolean) result.get("selected"));
        assertEquals("", result.get("projectRoot"));
    }
}
