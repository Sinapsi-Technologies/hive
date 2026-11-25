package io.sinapsi.hive.mcp.dependencies.application;

import io.sinapsi.hive.mcp.dependencies.domain.ArtifactCoordinates;
import io.sinapsi.hive.mcp.dependencies.domain.ArtifactVersion;

import java.io.IOException;
import java.util.List;

public interface ArtifactCatalog {

    String provider();

    List<ArtifactVersion> search(String query) throws IOException, InterruptedException;

    List<ArtifactVersion> versions(ArtifactCoordinates coordinates) throws IOException, InterruptedException;
}
