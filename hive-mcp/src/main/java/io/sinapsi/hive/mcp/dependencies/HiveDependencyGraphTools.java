package io.sinapsi.hive.mcp.dependencies;

import io.sinapsi.hive.mcp.dependencies.infrastructure.DependencyGraphResult;
import io.sinapsi.hive.mcp.dependencies.infrastructure.MavenDependencyGraph;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.sinapsi.hive.mcp.project.HiveProjectContext;

import java.io.IOException;
import java.nio.file.Path;

@Component
public final class HiveDependencyGraphTools {

    private final MavenDependencyGraph graph;

    private final HiveProjectContext projectContext;

    public HiveDependencyGraphTools(
            MavenDependencyGraph graph,
            HiveProjectContext projectContext) {
        this.graph = graph;
        this.projectContext = projectContext;
    }

    @McpTool(
            name = "hive_dependency_graph",
            description = "Resolve the configured Maven project and return its dependency graph. Use scope=compile, runtime, or test to focus the graph; omit scope to include all Maven scopes.")
    public DependencyGraphResult dependencyGraph(String scope) throws IOException, InterruptedException {
        return graph.scan(projectContext.current(), scope);
    }
}
