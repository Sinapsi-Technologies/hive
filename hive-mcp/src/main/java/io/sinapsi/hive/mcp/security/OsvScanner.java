package io.sinapsi.hive.mcp.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@EnableConfigurationProperties(OsvScannerProperties.class)
public final class OsvScanner {

    private final OsvProcessRunner runner;

    private final OsvScannerProperties properties;

    private final ObjectMapper mapper;

    @Autowired
    public OsvScanner(OsvScannerProperties properties, ObjectMapper mapper) {
        this((command, workingDirectory, timeout) -> {
            Process process = new ProcessBuilder(command)
                    .directory(workingDirectory.toFile())
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new OsvProcessRunner.ProcessResult(-1, "", true);
            }
            return new OsvProcessRunner.ProcessResult(
                    process.exitValue(),
                    new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8),
                    false);
        }, properties, mapper);
    }

    public OsvScanner(OsvProcessRunner runner, OsvScannerProperties properties) {
        this(runner, properties, new ObjectMapper());
    }

    public OsvScanner(OsvProcessRunner runner, OsvScannerProperties properties, ObjectMapper mapper) {
        this.runner = runner;
        this.properties = properties;
        this.mapper = mapper;
    }

    public OsvScanResult scan(Path projectRoot) throws IOException, InterruptedException {
        if (!properties.isEnabled()) {
            return new OsvScanResult(OsvScanResult.Status.DISABLED, 0, List.of(), "OSV-Scanner disabled");
        }

        Path root = projectRoot.toAbsolutePath().normalize();
        if (!root.toFile().isDirectory()) {
            throw new IllegalArgumentException("Project root is not a directory: " + root);
        }

        List<String> command = new ArrayList<>(List.of(
                resolveExecutable(), "scan", "source", "--format", "json"));
        if (properties.isRecursive()) {
            command.add("--recursive");
        }
        command.add(root.toString());

        OsvProcessRunner.ProcessResult result = runner.run(
                List.copyOf(command),
                root,
                Duration.ofMillis(properties.getTimeoutMillis()));
        if (result.timedOut()) {
            return new OsvScanResult(OsvScanResult.Status.TIMEOUT, result.exitCode(), List.of(),
                    "OSV-Scanner timed out");
        }
        if (result.exitCode() == 127) {
            return new OsvScanResult(OsvScanResult.Status.UNAVAILABLE, result.exitCode(), List.of(),
                    "OSV-Scanner executable not found: " + properties.getExecutable()
                            + ". Configure OSV_SCANNER_EXECUTABLE with an absolute path.");
        }

        List<OsvFinding> findings;
        try {
            findings = parseFindings(result.output());
        } catch (IOException exception) {
            return new OsvScanResult(OsvScanResult.Status.UNAVAILABLE, result.exitCode(), List.of(),
                    "OSV-Scanner returned non-JSON output: " + exception.getMessage());
        }
        boolean clean = result.exitCode() == 0 && findings.isEmpty();
        return new OsvScanResult(
                clean ? OsvScanResult.Status.PASS : OsvScanResult.Status.FAIL,
                result.exitCode(),
                findings,
                clean ? "" : findings.isEmpty()
                        ? "OSV-Scanner exited with code " + result.exitCode()
                        : "OSV-Scanner reported " + findings.size() + " vulnerability finding(s)");
    }

    private List<OsvFinding> parseFindings(String output) throws IOException {
        if (output == null || output.isBlank()) {
            return List.of();
        }

        JsonNode results = mapper.readTree(jsonPayload(output)).path("results");
        List<OsvFinding> findings = new ArrayList<>();
        for (JsonNode result : results) {
            String source = result.path("source").path("path").asText("");
            for (JsonNode packageResult : result.path("packages")) {
                JsonNode dependency = packageResult.path("package");
                String packageName = dependency.path("name").asText("");
                String version = dependency.path("version").asText("");
                String ecosystem = dependency.path("ecosystem").asText("");
                for (JsonNode vulnerability : packageResult.path("vulnerabilities")) {
                    findings.add(new OsvFinding(
                            packageName,
                            version,
                            ecosystem,
                            vulnerability.path("id").asText(""),
                            severity(vulnerability),
                            fixedVersion(vulnerability),
                            source));
                }
            }
        }
        return List.copyOf(findings);
    }

    private String jsonPayload(String output) throws IOException {
        int start = output.indexOf('{');
        int end = output.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IOException("JSON document not found in scanner output");
        }
        return output.substring(start, end + 1);
    }

    private String resolveExecutable() {
        String configured = properties.getExecutable();
        if (!"osv-scanner".equals(configured)) {
            return configured;
        }
        for (String candidate : List.of(
                "/opt/homebrew/bin/osv-scanner",
                "/usr/local/bin/osv-scanner")) {
            if (Files.isExecutable(Path.of(candidate))) {
                return candidate;
            }
        }
        return configured;
    }

    private String severity(JsonNode vulnerability) {
        JsonNode databaseSpecific = vulnerability.path("database_specific");
        return databaseSpecific.path("severity").asText(
                vulnerability.path("severity").path(0).path("type").asText("UNKNOWN"));
    }

    private String fixedVersion(JsonNode vulnerability) {
        for (JsonNode affected : vulnerability.path("affected")) {
            for (JsonNode event : affected.path("ranges").path(0).path("events")) {
                if (event.hasNonNull("fixed")) {
                    return event.get("fixed").asText();
                }
            }
        }
        return "";
    }
}
