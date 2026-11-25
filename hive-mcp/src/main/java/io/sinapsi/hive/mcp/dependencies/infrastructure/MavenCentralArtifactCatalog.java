package io.sinapsi.hive.mcp.dependencies.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sinapsi.hive.mcp.dependencies.application.ArtifactCatalog;
import io.sinapsi.hive.mcp.dependencies.domain.ArtifactCoordinates;
import io.sinapsi.hive.mcp.dependencies.domain.ArtifactVersion;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

@Component
public final class MavenCentralArtifactCatalog implements ArtifactCatalog {

    private final MavenCentralSearchClient client;

    private final ObjectMapper mapper;

    @Autowired
    public MavenCentralArtifactCatalog(ObjectMapper mapper) {
        this(
                (query, core) -> {
                    return resilient(
                            () -> RestClient.builder()
                                    .baseUrl("https://search.maven.org")
                                    .build()
                                    .get()
                                    .uri(uriBuilder -> uriBuilder
                                            .path("/solrsearch/select")
                                            .queryParam("q", query)
                            .queryParam("rows", 20)
                            .queryParamIfPresent("core", java.util.Optional.ofNullable(core))
                                            .queryParam("wt", "json")
                                            .build())
                                    .retrieve()
                                    .body(String.class));
                },
                mapper);
    }

    private static String resilient(CheckedSupplier<String> request) throws IOException, InterruptedException {
        Retry retry = Retry.ofDefaults("maven-central");
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("maven-central");
        CheckedSupplier<String> decorated = CircuitBreaker.decorateCheckedSupplier(
                circuitBreaker,
                Retry.decorateCheckedSupplier(retry, request));

        try {
            return decorated.get();
        } catch (IOException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw exception;
        } catch (Throwable exception) {
            throw new IOException("Maven Central request failed", exception);
        }
    }

    public MavenCentralArtifactCatalog(MavenCentralSearchClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public String provider() {
        return "maven-central";
    }

    @Override
    public List<ArtifactVersion> search(String query) throws IOException, InterruptedException {
        return parse(client.search(query, null));
    }

    @Override
    public List<ArtifactVersion> versions(ArtifactCoordinates coordinates)
            throws IOException, InterruptedException {
        return parse(client.search(
                "g:" + coordinates.groupId() + " AND a:" + coordinates.artifactId(),
                "gav"));
    }

    private List<ArtifactVersion> parse(String json) throws IOException {
        JsonNode docs = mapper.readTree(json).path("response").path("docs");
        List<ArtifactVersion> result = new ArrayList<>();

        for (JsonNode doc : docs) {
            String groupId = text(doc, "g");
            String artifactId = text(doc, "a");
            String version = doc.hasNonNull("v")
                    ? doc.get("v").asText()
                    : text(doc, "latestVersion");

            if (!groupId.isBlank() && !artifactId.isBlank() && !version.isBlank()) {
                result.add(new ArtifactVersion(new ArtifactCoordinates(groupId, artifactId), version));
            }
        }

        return List.copyOf(result);
    }

    private String text(JsonNode node, String field) {
        return node.path(field).asText("");
    }

}
