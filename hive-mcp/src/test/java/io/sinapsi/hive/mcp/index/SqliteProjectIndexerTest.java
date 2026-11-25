package io.sinapsi.hive.mcp.index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteProjectIndexerTest {

    @Test
    void indexesInheritanceCallsAndTestCoverage(@TempDir Path project) throws Exception {
        Path source = project.resolve("src/main/java/example");
        Files.createDirectories(source);
        Files.writeString(source.resolve("Port.java"), "package example; public interface Port {}\n");
        Files.writeString(source.resolve("Service.java"), "package example; public class Service implements Port { public void run() { helper(); } private void helper() {} }\n");
        Files.writeString(source.resolve("ServiceTest.java"), "package example; public class ServiceTest { void test() { new Service().run(); } }\n");

        SqliteProjectIndexer indexer = new SqliteProjectIndexer(root -> java.util.List.of());
        indexer.index(project);
        ProjectIndexSummary first = indexer.summary(project);
        indexer.index(project);
        ProjectIndexSummary second = indexer.summary(project);

        assertTrue(first.files() == second.files());
        assertTrue(first.relations() == second.relations());
        assertTrue(indexer.relations(project, "Service").stream().anyMatch(relation ->
                relation.relationType().equals("IMPLEMENTS") && relation.target().equals("Port")));
        assertTrue(indexer.relations(project, "Service").stream().anyMatch(relation ->
                relation.relationType().equals("CALLS_RESOLVED") && relation.target().contains("helper")));
        assertTrue(indexer.relations(project, "ServiceTest").stream().anyMatch(relation ->
                relation.relationType().equals("TEST_COVERS") && relation.target().equals("Service")));
        assertTrue(indexer.usedBy(project, "example.Service.helper()").stream().anyMatch(relation ->
                relation.source().equals("example.Service")));
        assertTrue(indexer.sourceContext(project, "Service", 1).source().contains("class Service"));
    }

    @Test
    void findsClassesImportingAnEnum(@TempDir Path project) throws Exception {
        Path source = project.resolve("src/main/java/example");
        Files.createDirectories(source);
        Files.writeString(source.resolve("Status.java"), "package example; public enum Status { OPEN }");
        Files.writeString(source.resolve("Order.java"), "package example; import example.Status; public class Order { Status status; }");

        SqliteProjectIndexer indexer = new SqliteProjectIndexer(root -> java.util.List.of());
        indexer.index(project);

        assertTrue(indexer.importedBy(project, "Status").stream().anyMatch(relation ->
                relation.source().equals("example.Order")
                        && relation.target().equals("example.Status")));
    }

    @Test
    void indexesOnlyRequestedFiles(@TempDir Path project) throws Exception {
        Path source = project.resolve("src/main/java/example");
        Files.createDirectories(source);
        Files.writeString(source.resolve("Status.java"), "package example; public enum Status { OPEN }");
        Files.writeString(source.resolve("Order.java"), "package example; import example.Status; public class Order { Status status; }");

        SqliteProjectIndexer indexer = new SqliteProjectIndexer(root -> java.util.List.of());
        indexer.indexFiles(project, java.util.List.of("src/main/java/example/Status.java"));
        assertTrue(indexer.summary(project).files() == 1);

        indexer.indexFiles(project, java.util.List.of("src/main/java/example/Order.java"));
        assertTrue(indexer.importedBy(project, "Status").stream().anyMatch(relation ->
                relation.source().equals("example.Order")));
    }
}
