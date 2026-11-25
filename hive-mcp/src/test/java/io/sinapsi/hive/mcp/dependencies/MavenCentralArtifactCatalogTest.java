package io.sinapsi.hive.mcp.dependencies;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sinapsi.hive.mcp.dependencies.infrastructure.MavenCentralArtifactCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MavenCentralArtifactCatalogTest {

    @Test
    void parsesSearchResultsIntoProviderIndependentCoordinates() throws Exception {
        String json = """
                {"response":{"docs":[
                  {"g":"org.springframework","a":"spring-core","latestVersion":"6.2.0"},
                  {"g":"org.junit.jupiter","a":"junit-jupiter","v":"5.10.2"}
                ]}}
                """;

        MavenCentralArtifactCatalog catalog = new MavenCentralArtifactCatalog((query, core) -> json, new ObjectMapper());

        assertEquals(
                List.of("org.springframework:spring-core:6.2.0", "org.junit.jupiter:junit-jupiter:5.10.2"),
                catalog.search("spring" ).stream().map(version -> version.gav()).toList());
    }
}
