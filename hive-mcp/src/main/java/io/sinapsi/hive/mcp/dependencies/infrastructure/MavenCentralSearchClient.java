package io.sinapsi.hive.mcp.dependencies.infrastructure;

import java.io.IOException;

@FunctionalInterface
public interface MavenCentralSearchClient {

    String search(String query, String core) throws IOException, InterruptedException;
}
