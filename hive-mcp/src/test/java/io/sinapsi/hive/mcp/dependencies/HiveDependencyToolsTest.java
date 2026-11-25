package io.sinapsi.hive.mcp.dependencies;

import io.sinapsi.hive.mcp.dependencies.application.ArtifactCatalog;
import io.sinapsi.hive.mcp.dependencies.domain.ArtifactCoordinates;
import io.sinapsi.hive.mcp.dependencies.domain.ArtifactVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HiveDependencyToolsTest {

    @Test
    void addsPinnedDependencyToProjectPom(@TempDir Path project) throws Exception {
        Files.writeString(project.resolve("hive.yml"), "basePackage: com.example.test\n");
        Files.writeString(project.resolve("pom.xml"), """
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example.test</groupId>
                  <artifactId>demo</artifactId>
                  <version>0.1.0</version>
                </project>
                """);

        ArtifactCatalog catalog = new ArtifactCatalog() {
            @Override
            public String provider() {
                return "test";
            }

            @Override
            public List<ArtifactVersion> search(String query) {
                return List.of();
            }

            @Override
            public List<ArtifactVersion> versions(ArtifactCoordinates coordinates) {
                return List.of();
            }
        };
        HiveDependencyTools tools = new HiveDependencyTools(catalog, project);

        Map<String, Object> result = tools.addDependency(
                "org.junit.jupiter", "junit-jupiter", "5.10.2", "test");

        String pom = Files.readString(project.resolve("pom.xml"));
        assertEquals(true, result.get("added"));
        assertTrue(pom.contains("<groupId>org.junit.jupiter</groupId>"));
        assertTrue(pom.contains("<artifactId>junit-jupiter</artifactId>"));
        assertTrue(pom.contains("<version>5.10.2</version>"));
        assertTrue(pom.contains("<scope>test</scope>"));
    }

    @Test
    void rollsBackPomWhenVerificationFails(@TempDir Path project) throws Exception {
        Files.writeString(project.resolve("hive.yml"), "basePackage: com.example.test\n");
        String originalPom = """
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.example.test</groupId>
                  <artifactId>demo</artifactId>
                  <version>0.1.0</version>
                </project>
                """;
        Files.writeString(project.resolve("pom.xml"), originalPom);

        ArtifactCatalog catalog = new EmptyCatalog();
        DependencyChangeVerifier verifier = path ->
                new DependencyChangeVerifier.VerificationResult("FAIL", "PASS", false, "tests failed");

        HiveDependencyTools tools = new HiveDependencyTools(catalog, project, verifier);

        Map<String, Object> result = tools.addDependency(
                "org.example", "broken", "1.0.0", "compile", true);

        assertEquals(true, result.get("rolledBack"));
        assertEquals(originalPom, Files.readString(project.resolve("pom.xml")));
    }

    private static final class EmptyCatalog implements ArtifactCatalog {

        @Override
        public String provider() {
            return "test";
        }

        @Override
        public List<ArtifactVersion> search(String query) {
            return List.of();
        }

        @Override
        public List<ArtifactVersion> versions(ArtifactCoordinates coordinates) {
            return List.of();
        }
    }
}
