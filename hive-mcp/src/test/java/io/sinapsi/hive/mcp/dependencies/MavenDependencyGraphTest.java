package io.sinapsi.hive.mcp.dependencies;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sinapsi.hive.mcp.dependencies.infrastructure.DependencyGraphResult;
import io.sinapsi.hive.mcp.dependencies.infrastructure.MavenDependencyGraph;
import io.sinapsi.hive.mcp.dependencies.infrastructure.MavenDependencyGraphProperties;
import io.sinapsi.hive.mcp.dependencies.infrastructure.MavenDependencyProcessRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenDependencyGraphTest {

    @Test
    void mapsResolvedMavenTreeToMinimalGraph(@TempDir Path project) throws Exception {
        Files.writeString(project.resolve("pom.xml"), "<project/>\n");
        MavenDependencyGraphProperties properties = new MavenDependencyGraphProperties();
        properties.setPluginVersion("3.11.0");
        MavenDependencyProcessRunner runner = (command, root, timeout) -> {
            assertEquals("-Dscope=test", command.getLast());
            assertEquals(Duration.ofMillis(120_000), timeout);
            return new MavenDependencyProcessRunner.ProcessResult(0, """
                    {"groupId":"com.example","artifactId":"app","version":"1.0.0","scope":"","optional":false,"children":[{"groupId":"org.example","artifactId":"library","version":"2.0.0","scope":"test","optional":false,"children":[]}]}
                    """, false);
        };

        MavenDependencyGraph graph = new MavenDependencyGraph(runner, properties, new ObjectMapper());

        DependencyGraphResult result = graph.scan(project, "test");

        assertEquals(DependencyGraphResult.Status.PASS, result.status());
        assertEquals("com.example", result.root().groupId());
        assertEquals("org.example", result.root().children().getFirst().groupId());
        assertTrue(result.error().isBlank());
    }

    @Test
    void ignoresMavenProgressTextBeforeJson(@TempDir Path project) throws Exception {
        Files.writeString(project.resolve("pom.xml"), "<project/>\n");
        MavenDependencyGraphProperties properties = new MavenDependencyGraphProperties();
        MavenDependencyGraph graph = new MavenDependencyGraph(
                (command, root, timeout) -> new MavenDependencyProcessRunner.ProcessResult(
                        0, "Scanning for projects...\n{\"groupId\":\"com.example\",\"artifactId\":\"app\",\"version\":\"1.0.0\",\"children\":[]}", false),
                properties, new ObjectMapper());

        assertEquals(DependencyGraphResult.Status.PASS, graph.scan(project, null).status());
    }
}
