package io.sinapsi.hive.mcp.dependencies.domain;

public record ArtifactVersion(ArtifactCoordinates coordinates, String version) {

    public String gav() {
        return coordinates.gav() + ":" + version;
    }
}
