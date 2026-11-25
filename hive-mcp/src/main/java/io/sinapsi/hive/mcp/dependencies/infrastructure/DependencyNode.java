package io.sinapsi.hive.mcp.dependencies.infrastructure;

import java.util.List;

public record DependencyNode(
        String groupId,
        String artifactId,
        String version,
        String scope,
        boolean optional,
        List<DependencyNode> children) {

    public DependencyNode {
        children = List.copyOf(children == null ? List.of() : children);
    }
}
