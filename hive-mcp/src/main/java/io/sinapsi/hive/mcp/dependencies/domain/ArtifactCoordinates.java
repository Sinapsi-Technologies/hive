package io.sinapsi.hive.mcp.dependencies.domain;

import java.util.Objects;

public record ArtifactCoordinates(String groupId, String artifactId) {

    public ArtifactCoordinates {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(artifactId, "artifactId must not be null");

        if (groupId.isBlank() || artifactId.isBlank()) {
            throw new IllegalArgumentException("groupId and artifactId must not be blank");
        }
    }

    public String gav() {
        return groupId + ":" + artifactId;
    }
}
