package io.sinapsi.hive.mcp.dependencies.infrastructure;

public record DependencyGraphResult(Status status, int exitCode, DependencyNode root, String error) {

    public enum Status {
        PASS,
        FAIL,
        TIMEOUT
    }
}
