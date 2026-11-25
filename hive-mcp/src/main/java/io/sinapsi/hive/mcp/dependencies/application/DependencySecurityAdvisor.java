package io.sinapsi.hive.mcp.dependencies.application;

import io.sinapsi.hive.mcp.dependencies.domain.ArtifactVersion;

import java.io.IOException;

public interface DependencySecurityAdvisor {

    String provider();

    SecurityAssessment assess(ArtifactVersion dependency) throws IOException, InterruptedException;
}
