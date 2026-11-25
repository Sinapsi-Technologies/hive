package io.sinapsi.hive.mcp.dependencies.infrastructure;

import io.sinapsi.hive.mcp.dependencies.domain.ArtifactVersion;

import java.io.IOException;

@FunctionalInterface
public interface SonatypeGuideClient {

    String vulnerabilities(ArtifactVersion dependency) throws IOException, InterruptedException;
}
