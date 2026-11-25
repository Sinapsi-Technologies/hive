package io.sinapsi.hive.mcp.index;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.sinapsi.hive.mcp.project.HiveProjectContext;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

@Component
public final class HiveProjectIndexTools {
    private static final Logger log = LoggerFactory.getLogger(HiveProjectIndexTools.class);

    private final SqliteProjectIndexer indexer;

    private final HiveProjectContext projectContext;

    public HiveProjectIndexTools(
            SqliteProjectIndexer indexer,
            HiveProjectContext projectContext) {
        this.indexer = indexer;
        this.projectContext = projectContext;
    }

    @McpTool(
            name = "hive_analyze_project",
            description = "Scan the configured Java project and build a SQLite index of files, types, methods, and imports. Run this before asking for legacy architecture or migration context.")
    public ProjectIndexSummary analyzeProject() throws IOException, SQLException {
        Path root = projectContext.current();
        long started = System.nanoTime();
        log.info("Hive project analysis started for {}", root);
        try {
            ProjectIndexSummary summary = indexer.index(root);
            log.info("Hive project analysis completed for {}: {} files, {} relations, {} ms",
                    root, summary.files(), summary.relations(), (System.nanoTime() - started) / 1_000_000);
            return summary;
        } catch (IOException | SQLException exception) {
            log.error("Hive project analysis failed for {} after {} ms: {}", root,
                    (System.nanoTime() - started) / 1_000_000, exception.getMessage(), exception);
            throw exception;
        }
    }

    @McpTool(
            name = "hive_index_files",
            description = "Incrementally index only the listed Java files relative to the selected project root. Use this immediately after Hive generates or modifies files; paths must stay inside the project and unchanged files are skipped by SHA-256.")
    public ProjectIndexSummary indexFiles(List<String> paths) throws IOException, SQLException {
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException("paths must contain at least one project-relative Java path");
        }
        return indexer.indexFiles(projectContext.current(), paths);
    }

    @McpTool(
            name = "hive_project_index_summary",
            description = "Return counts and the location of the current Hive project index without rescanning source files.")
    public ProjectIndexSummary indexSummary() throws SQLException {
        return indexer.summary(projectContext.current());
    }

    @McpTool(
            name = "hive_get_symbol_relations",
            description = "Read indexed relationships for a Java type, including imports, inheritance, and implemented interfaces. Run hive_analyze_project first.")
    public List<ProjectRelation> symbolRelations(String symbolName) throws SQLException {
        if (symbolName == null || symbolName.isBlank()) {
            throw new IllegalArgumentException("symbolName must not be blank");
        }
        return indexer.relations(projectContext.current(), symbolName.trim());
    }

    @McpTool(
            name = "hive_get_used_by",
            description = "Find indexed classes that call a resolved method or symbol. Use it before changing a legacy component to estimate the impact of the migration.")
    public List<ProjectRelation> usedBy(String symbolName) throws SQLException {
        if (symbolName == null || symbolName.isBlank()) {
            throw new IllegalArgumentException("symbolName must not be blank");
        }
        return indexer.usedBy(projectContext.current(), symbolName.trim());
    }

    @McpTool(
            name = "hive_symbol_imported_by",
            description = "Return only the indexed classes that import a symbol, such as an enum or value object. Run hive_analyze_project after generation or source changes before querying this graph relation.")
    public List<ProjectRelation> importedBy(String symbolName) throws SQLException {
        if (symbolName == null || symbolName.isBlank()) {
            throw new IllegalArgumentException("symbolName must not be blank");
        }
        return indexer.importedBy(projectContext.current(), symbolName.trim());
    }

    @McpTool(
            name = "hive_get_source_context",
            description = "Return only the indexed source fragment around a class or method. Use it after querying the index instead of loading an entire source file into the agent context.")
    public SourceContext sourceContext(String symbolName, int linesAround) throws IOException, SQLException {
        if (symbolName == null || symbolName.isBlank()) {
            throw new IllegalArgumentException("symbolName must not be blank");
        }
        return indexer.sourceContext(projectContext.current(), symbolName.trim(), linesAround);
    }
}
