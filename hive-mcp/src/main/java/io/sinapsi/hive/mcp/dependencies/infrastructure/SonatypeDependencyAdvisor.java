package io.sinapsi.hive.mcp.dependencies.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sinapsi.hive.mcp.dependencies.application.DependencySecurityAdvisor;
import io.sinapsi.hive.mcp.dependencies.application.SecurityAdvisory;
import io.sinapsi.hive.mcp.dependencies.application.SecurityAssessment;
import io.sinapsi.hive.mcp.dependencies.domain.ArtifactVersion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "hive.security.sonatype", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SonatypeSecurityProperties.class)
public final class SonatypeDependencyAdvisor implements DependencySecurityAdvisor {

    private final SonatypeGuideClient client;

    private final ObjectMapper mapper;

    private final SonatypeSecurityProperties properties;

    @Autowired
    public SonatypeDependencyAdvisor(ObjectMapper mapper, SonatypeSecurityProperties properties) {
        this(
                dependency -> {
                    if (properties.getToken() == null || properties.getToken().isBlank()) {
                        throw new IllegalStateException("Sonatype token is required when the advisor is enabled");
                    }

                    String purl = "pkg:maven/"
                            + dependency.coordinates().groupId()
                            + "/"
                            + dependency.coordinates().artifactId()
                            + "@"
                            + dependency.version();
                    String query = URLEncoder.encode(purl, StandardCharsets.UTF_8);
                    URI uri = URI.create(properties.getBaseUrl() + "/components/vulnerabilities?purl=" + query);
                    HttpRequest request = HttpRequest.newBuilder(uri)
                            .timeout(Duration.ofMillis(properties.getTimeoutMillis()))
                            .header("Authorization", "Bearer " + properties.getToken())
                            .header("Accept", "application/json")
                            .GET()
                            .build();
                    HttpResponse<String> response = HttpClient.newHttpClient()
                            .send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() >= 400) {
                        throw new IOException("Sonatype Guide returned HTTP " + response.statusCode());
                    }

                    return response.body();
                },
                mapper,
                properties);
    }

    public SonatypeDependencyAdvisor(
            SonatypeGuideClient client,
            ObjectMapper mapper,
            SonatypeSecurityProperties properties) {
        this.client = client;
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    public String provider() {
        return "sonatype-guide";
    }

    @Override
    public SecurityAssessment assess(ArtifactVersion dependency) throws IOException, InterruptedException {
        if (!properties.isEnabled()) {
            return new SecurityAssessment(provider(), SecurityAssessment.Status.UNKNOWN, List.of(), "Sonatype advisor disabled");
        }

        try {
            List<SecurityAdvisory> advisories = parse(client.vulnerabilities(dependency));
            SecurityAssessment.Status status = advisories.isEmpty()
                    ? SecurityAssessment.Status.PASS
                    : advisories.stream().anyMatch(advisory -> severity(advisory.severity()) >= severity(properties.getFailSeverity()))
                            ? SecurityAssessment.Status.FAIL
                            : SecurityAssessment.Status.WARN;
            return new SecurityAssessment(provider(), status, advisories,
                    advisories.isEmpty() ? "No known vulnerabilities" : "Known vulnerabilities found");
        } catch (RuntimeException | IOException | InterruptedException exception) {
            if (properties.isFailOnUnavailable()) {
                throw exception;
            }

            return new SecurityAssessment(provider(), SecurityAssessment.Status.UNKNOWN, List.of(),
                    "Sonatype unavailable: " + exception.getMessage());
        }
    }

    private List<SecurityAdvisory> parse(String json) throws IOException {
        JsonNode root = mapper.readTree(json);
        JsonNode vulnerabilities = root.isArray()
                ? root
                : root.path("vulnerabilities").isArray()
                        ? root.path("vulnerabilities")
                        : root.path("data");
        List<SecurityAdvisory> advisories = new ArrayList<>();

        if (!vulnerabilities.isArray()) {
            return List.of();
        }

        for (JsonNode vulnerability : vulnerabilities) {
            String identifier = first(vulnerability, "identifier", "id", "refId");
            String severity = vulnerability.path("severity").isObject()
                    ? first(vulnerability.path("severity"), "level", "name", "value")
                    : first(vulnerability, "severity", "mainSeverity", "threatLevel");
            String fixedVersion = first(vulnerability, "fixedVersion", "fixVersion", "recommendation");
            String url = first(vulnerability, "url", "vulnerabilityLink", "link");
            advisories.add(new SecurityAdvisory(identifier, severity, fixedVersion, url));
        }

        return List.copyOf(advisories);
    }

    private String first(JsonNode node, String... fields) {
        for (String field : fields) {
            if (node.hasNonNull(field)) {
                JsonNode value = node.get(field);
                if (value.isTextual() || value.isNumber()) {
                    return value.asText();
                }

                if (value.isObject()) {
                    String nested = first(value, "value", "name", "level", "score");
                    if (!nested.isBlank()) {
                        return nested;
                    }
                }
            }
        }

        return "";
    }

    private int severity(String value) {
        return switch (value == null ? "" : value.toUpperCase()) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM", "MODERATE" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}
