package io.sinapsi.hive.mcp.index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.file.Path;

@EnabledIfSystemProperty(named = "hive.index.preview", matches = "true")
class ProjectIndexPreviewTest {

    @Test
    void indexCurrentWorkspace() throws Exception {
        ProjectIndexSummary summary = new SqliteProjectIndexer().index(Path.of("."));
        System.out.println("HIVE_INDEX_PREVIEW=" + summary);
    }
}
