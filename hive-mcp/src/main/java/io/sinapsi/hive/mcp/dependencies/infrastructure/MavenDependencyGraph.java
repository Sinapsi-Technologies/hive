package io.sinapsi.hive.mcp.dependencies.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@EnableConfigurationProperties(MavenDependencyGraphProperties.class)
public final class MavenDependencyGraph {

    private final MavenDependencyProcessRunner runner;

    private final MavenDependencyGraphProperties properties;

    private final ObjectMapper mapper;

    @Autowired
    public MavenDependencyGraph(MavenDependencyGraphProperties properties, ObjectMapper mapper) {
        this((command, workingDirectory, timeout) -> {
            Process process = new ProcessBuilder(command)
                    .directory(workingDirectory.toFile())
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new MavenDependencyProcessRunner.ProcessResult(-1, "", true);
            }
            return new MavenDependencyProcessRunner.ProcessResult(
                    process.exitValue(),
                    new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8),
                    false);
        }, properties, mapper);
    }

    public MavenDependencyGraph(
            MavenDependencyProcessRunner runner,
            MavenDependencyGraphProperties properties,
            ObjectMapper mapper) {
        this.runner = runner;
        this.properties = properties;
        this.mapper = mapper;
    }

    public DependencyGraphResult scan(Path projectRoot, String scope) throws IOException, InterruptedException {
        Path root = projectRoot.toAbsolutePath().normalize();
        if (!root.resolve("pom.xml").toFile().isFile()) {
            throw new IllegalArgumentException("pom.xml not found in project root: " + root);
        }

        String normalizedScope = scope == null || scope.isBlank() ? null : scope.trim().toLowerCase();
        if (normalizedScope != null && !List.of("compile", "provided", "runtime", "test", "system", "import")
                .contains(normalizedScope)) {
            throw new IllegalArgumentException("Unsupported Maven scope: " + scope);
        }

        List<String> command = new ArrayList<>(List.of(
                properties.getExecutable(),
                "-q",
                "org.apache.maven.plugins:maven-dependency-plugin:" + properties.getPluginVersion() + ":tree",
                "-DoutputType=json"));
        if (normalizedScope != null) {
            command.add("-Dscope=" + normalizedScope);
        }

        MavenDependencyProcessRunner.ProcessResult result = runner.run(
                List.copyOf(command),
                root,
                Duration.ofMillis(properties.getTimeoutMillis()));
        if (result.timedOut()) {
            return new DependencyGraphResult(DependencyGraphResult.Status.TIMEOUT, result.exitCode(), null,
                    "Maven dependency graph timed out");
        }
        if (result.exitCode() != 0) {
            return new DependencyGraphResult(DependencyGraphResult.Status.FAIL, result.exitCode(), null,
                    "Maven dependency graph failed");
        }

        try {
            return new DependencyGraphResult(
                    DependencyGraphResult.Status.PASS,
                    result.exitCode(),
                    parse(result.output()),
                    "");
        } catch (IOException exception) {
            return new DependencyGraphResult(
                    DependencyGraphResult.Status.FAIL,
                    result.exitCode(),
                    null,
                    "Maven dependency graph returned non-JSON output: " + exception.getMessage());
        }
    }

    private DependencyNode parse(String output) throws IOException {
        int start = output.indexOf('{');
        int end = output.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IOException("JSON document not found in Maven output");
        }
        JsonNode node = mapper.readTree(output.substring(start, end + 1));
        return parseNode(node);
    }

    private DependencyNode parseNode(JsonNode node) {
        List<DependencyNode> children = new ArrayList<>();
        for (JsonNode child : node.path("children")) {
            children.add(parseNode(child));
        }
        return new DependencyNode(
                node.path("groupId").asText(""),
                node.path("artifactId").asText(""),
                node.path("version").asText(""),
                node.path("scope").asText(""),
                node.path("optional").asBoolean(false),
                children);
    }
}
